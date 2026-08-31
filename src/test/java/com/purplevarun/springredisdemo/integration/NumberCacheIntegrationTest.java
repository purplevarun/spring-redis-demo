package com.purplevarun.springredisdemo.integration;

import com.purplevarun.springredisdemo.dto.NumberRequest;
import com.purplevarun.springredisdemo.service.CacheStatsService;
import com.purplevarun.springredisdemo.service.NumberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class NumberCacheIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("spring_redis_demo")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private NumberService numberService;

    @Autowired
    private CacheStatsService statsService;

    @BeforeEach
    void clearCacheAndStats() {
        // ensure a clean slate between tests
        statsService.reset();
        numberService.getAllNumbers(); // prime or clear via evict
        statsService.reset();
    }

    @Test
    void firstGetIsMiss_secondGetIsHit() {
        numberService.getAllNumbers();
        assertThat(statsService.getLastCacheStatus()).isEqualTo("MISS");

        numberService.getAllNumbers();
        assertThat(statsService.getLastCacheStatus()).isEqualTo("HIT");
    }

    @Test
    void createNumber_evictsCacheSoNextGetIsMiss() {
        // warm up the cache
        numberService.getAllNumbers();
        statsService.getLastCacheStatus(); // discard the MISS status

        // write evicts the cache
        numberService.createNumber(new NumberRequest(123));

        // next read should be a MISS again
        numberService.getAllNumbers();
        assertThat(statsService.getLastCacheStatus()).isEqualTo("MISS");
    }

    @Test
    void statsAccumulateAcrossMultipleCalls() {
        numberService.getAllNumbers(); // MISS
        numberService.getAllNumbers(); // HIT
        numberService.getAllNumbers(); // HIT

        assertThat(statsService.toResponse().getHits()).isGreaterThanOrEqualTo(2);
        assertThat(statsService.toResponse().getMisses()).isGreaterThanOrEqualTo(1);
        assertThat(statsService.toResponse().getHitRate()).isGreaterThan(0.0);
    }
}

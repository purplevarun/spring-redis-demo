package com.purplevarun.springredisdemo.integration;

import com.purplevarun.springredisdemo.dto.NumberRequest;
import com.purplevarun.springredisdemo.model.NumberEntry;
import com.purplevarun.springredisdemo.service.CacheStatsService;
import com.purplevarun.springredisdemo.service.NumberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
// override max-size to 3 so LRU eviction is observable with a small number of entries
@TestPropertySource(properties = "app.cache.max-size=3")
class NumberCacheIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("spring_redis_demo")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>("redis:8-alpine")
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
    void resetStats() {
        statsService.reset();
    }

    @Test
    void firstLookupIsMiss_secondIsHit() {
        NumberEntry entry = numberService.createNumber(new NumberRequest(42));

        numberService.getNumberById(entry.getId());
        assertThat(statsService.getLastCacheStatus()).isEqualTo("MISS");

        numberService.getNumberById(entry.getId());
        assertThat(statsService.getLastCacheStatus()).isEqualTo("HIT");
    }

    @Test
    void lruEviction_removesOldestEntry_whenMaxSizeExceeded() {
        NumberEntry e1 = numberService.createNumber(new NumberRequest(1));
        NumberEntry e2 = numberService.createNumber(new NumberRequest(2));
        NumberEntry e3 = numberService.createNumber(new NumberRequest(3));
        NumberEntry e4 = numberService.createNumber(new NumberRequest(4));

        // fill cache to max (3 entries)
        numberService.getNumberById(e1.getId()); // MISS — e1 in cache (LRU: e1)
        numberService.getNumberById(e2.getId()); // MISS — e2 in cache (LRU: e1,e2)
        numberService.getNumberById(e3.getId()); // MISS — e3 in cache (LRU: e1,e2,e3)
        statsService.reset();

        // 4th entry causes LRU eviction of e1
        numberService.getNumberById(e4.getId()); // MISS — evicts e1, cache: e2,e3,e4
        assertThat(statsService.toResponse().getEvictions()).isEqualTo(1);

        statsService.reset();
        // e1 was evicted so it should be a MISS again
        numberService.getNumberById(e1.getId());
        assertThat(statsService.getLastCacheStatus()).isEqualTo("MISS");
    }

    @Test
    void statsAccumulateAcrossMultipleLookups() {
        NumberEntry entry = numberService.createNumber(new NumberRequest(99));

        numberService.getNumberById(entry.getId()); // MISS
        numberService.getNumberById(entry.getId()); // HIT
        numberService.getNumberById(entry.getId()); // HIT

        assertThat(statsService.toResponse().getHits()).isGreaterThanOrEqualTo(2);
        assertThat(statsService.toResponse().getMisses()).isGreaterThanOrEqualTo(1);
        assertThat(statsService.toResponse().getHitRate()).isGreaterThan(0.0);
    }
}

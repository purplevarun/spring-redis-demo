package com.purplevarun.springredisdemo.service;

import com.purplevarun.springredisdemo.dto.CacheStatsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CacheStatsServiceTest {

    private CacheStatsService service;

    @BeforeEach
    void setUp() {
        service = new CacheStatsService();
    }

    @Test
    void recordHit_incrementsHitCount() {
        service.recordHit();
        assertThat(service.toResponse().getHits()).isEqualTo(1L);
    }

    @Test
    void recordMiss_incrementsMissCount() {
        service.recordMiss();
        assertThat(service.toResponse().getMisses()).isEqualTo(1L);
    }

    @Test
    void getLastCacheStatus_returnsHit_afterRecordHit() {
        service.recordHit();
        assertThat(service.getLastCacheStatus()).isEqualTo("HIT");
    }

    @Test
    void getLastCacheStatus_returnsMiss_afterRecordMiss() {
        service.recordMiss();
        assertThat(service.getLastCacheStatus()).isEqualTo("MISS");
    }

    @Test
    void getLastCacheStatus_returnsUnknown_whenNothingRecorded() {
        assertThat(service.getLastCacheStatus()).isEqualTo("UNKNOWN");
    }

    @Test
    void getLastCacheStatus_clearsThreadLocalAfterRead() {
        service.recordHit();
        service.getLastCacheStatus(); // first read clears it
        assertThat(service.getLastCacheStatus()).isEqualTo("UNKNOWN");
    }

    @Test
    void toResponse_calculatesHitRateCorrectly() {
        service.recordHit();
        service.recordHit();
        service.recordMiss();

        CacheStatsResponse response = service.toResponse();

        assertThat(response.getHits()).isEqualTo(2L);
        assertThat(response.getMisses()).isEqualTo(1L);
        assertThat(response.getHitRate()).isCloseTo(0.666, within(0.001));
    }

    @Test
    void toResponse_returnsZeroHitRate_whenNoRequestsMade() {
        CacheStatsResponse response = service.toResponse();
        assertThat(response.getHitRate()).isEqualTo(0.0);
    }

    @Test
    void reset_clearsCountersAndThreadLocal() {
        service.recordHit();
        service.recordMiss();
        service.reset();

        CacheStatsResponse response = service.toResponse();
        assertThat(response.getHits()).isZero();
        assertThat(response.getMisses()).isZero();
        assertThat(service.getLastCacheStatus()).isEqualTo("UNKNOWN");
    }
}

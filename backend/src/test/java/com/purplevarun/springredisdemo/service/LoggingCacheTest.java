package com.purplevarun.springredisdemo.service;

import com.purplevarun.springredisdemo.config.LoggingCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.support.SimpleValueWrapper;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingCacheTest {

    @Mock
    private Cache delegate;

    @Mock
    private CacheStatsService statsService;

    private LoggingCache loggingCache;

    @BeforeEach
    void setUp() {
        loggingCache = new LoggingCache(delegate, statsService, 10);
    }

    @Test
    void getName_delegatesToDelegate() {
        when(delegate.getName()).thenReturn("numbers");
        assertThat(loggingCache.getName()).isEqualTo("numbers");
    }

    @Test
    void getNativeCache_delegatesToDelegate() {
        Object native_ = new Object();
        when(delegate.getNativeCache()).thenReturn(native_);
        assertThat(loggingCache.getNativeCache()).isSameAs(native_);
    }

    @Test
    void get_byKey_recordsHit_whenValuePresent() {
        ValueWrapper wrapper = new SimpleValueWrapper("data");
        when(delegate.get("all")).thenReturn(wrapper);

        ValueWrapper result = loggingCache.get("all");

        assertThat(result).isEqualTo(wrapper);
        verify(statsService).recordHit();
        verify(statsService, never()).recordMiss();
    }

    @Test
    void get_byKey_recordsMiss_whenValueAbsent() {
        when(delegate.get("all")).thenReturn(null);

        ValueWrapper result = loggingCache.get("all");

        assertThat(result).isNull();
        verify(statsService).recordMiss();
        verify(statsService, never()).recordHit();
    }

    @Test
    void get_typed_recordsHit_whenValuePresent() {
        when(delegate.get("all", String.class)).thenReturn("data");

        String result = loggingCache.get("all", String.class);

        assertThat(result).isEqualTo("data");
        verify(statsService).recordHit();
    }

    @Test
    void get_typed_recordsMiss_whenValueAbsent() {
        when(delegate.get("all", String.class)).thenReturn(null);

        String result = loggingCache.get("all", String.class);

        assertThat(result).isNull();
        verify(statsService).recordMiss();
    }

    @Test
    @SuppressWarnings("unchecked")
    void get_withCallable_delegatesWithoutLogging() throws Exception {
        Callable<String> loader = () -> "loaded";
        when(delegate.get("all", loader)).thenReturn("loaded");

        String result = loggingCache.get("all", loader);

        assertThat(result).isEqualTo("loaded");
        verifyNoInteractions(statsService);
    }

    @Test
    void put_delegatesToDelegate() {
        loggingCache.put("all", "value");
        verify(delegate).put("all", "value");
    }

    @Test
    void putIfAbsent_delegatesToDelegate() {
        ValueWrapper wrapper = new SimpleValueWrapper("existing");
        when(delegate.putIfAbsent("all", "value")).thenReturn(wrapper);

        ValueWrapper result = loggingCache.putIfAbsent("all", "value");

        assertThat(result).isEqualTo(wrapper);
        verify(delegate).putIfAbsent("all", "value");
    }

    @Test
    void put_evictsLruEntry_whenMaxSizeExceeded() {
        LoggingCache bounded = new LoggingCache(delegate, statsService, 2);

        bounded.put("key1", "v1");
        bounded.put("key2", "v2");
        bounded.put("key3", "v3"); // should evict key1 (LRU)

        verify(delegate).evict("key1");
        verify(statsService).recordEviction();
    }

    @Test
    void put_doesNotEvict_whenUnderMaxSize() {
        LoggingCache bounded = new LoggingCache(delegate, statsService, 5);
        bounded.put("key1", "v1");
        bounded.put("key2", "v2");

        verify(delegate, never()).evict(any());
        verify(statsService, never()).recordEviction();
    }

    @Test
    void evict_removesKeyFromLruTracker() {
        LoggingCache bounded = new LoggingCache(delegate, statsService, 2);
        bounded.put("key1", "v1");
        bounded.put("key2", "v2");
        bounded.evict("key1");
        bounded.put("key3", "v3"); // key1 was removed from tracker, so no LRU-triggered eviction

        // direct evict was called for key1; no LRU eviction should have fired
        verify(statsService, never()).recordEviction();
    }

    @Test
    void clear_resetsLruTracker() {
        LoggingCache bounded = new LoggingCache(delegate, statsService, 2);
        bounded.put("key1", "v1");
        bounded.put("key2", "v2");
        bounded.clear();
        bounded.put("key3", "v3"); // after clear the tracker is empty, no eviction

        verify(statsService, never()).recordEviction();
    }

    @Test
    void evictIfPresent_delegatesToDelegate() {
        when(delegate.evictIfPresent("all")).thenReturn(true);
        assertThat(loggingCache.evictIfPresent("all")).isTrue();
    }

    @Test
    void clear_delegatesToDelegate() {
        loggingCache.clear();
        verify(delegate).clear();
    }

    @Test
    void invalidate_delegatesToDelegate() {
        when(delegate.invalidate()).thenReturn(true);
        assertThat(loggingCache.invalidate()).isTrue();
    }
}

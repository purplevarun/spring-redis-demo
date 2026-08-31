package com.purplevarun.springredisdemo.config;

import com.purplevarun.springredisdemo.service.CacheStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import java.util.concurrent.Callable;

/**
 * Wraps a delegate Cache to log HIT/MISS on every get and update CacheStatsService counters.
 */
public class LoggingCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(LoggingCache.class);

    private final Cache delegate;
    private final CacheStatsService statsService;

    public LoggingCache(Cache delegate, CacheStatsService statsService) {
        this.delegate = delegate;
        this.statsService = statsService;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper wrapper = delegate.get(key);
        if (wrapper != null) {
            log.info("Cache HIT  for key: {}", key);
            statsService.recordHit();
        } else {
            log.info("Cache MISS for key: {}", key);
            statsService.recordMiss();
        }
        return wrapper;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        T value = delegate.get(key, type);
        if (value != null) {
            log.info("Cache HIT  for key: {} (typed)", key);
            statsService.recordHit();
        } else {
            log.info("Cache MISS for key: {} (typed)", key);
            statsService.recordMiss();
        }
        return value;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        // sync=true path — delegate handles loading; no separate HIT/MISS tracking here
        return delegate.get(key, valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        delegate.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return delegate.putIfAbsent(key, value);
    }

    @Override
    public void evict(Object key) {
        delegate.evict(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return delegate.evictIfPresent(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean invalidate() {
        return delegate.invalidate();
    }
}

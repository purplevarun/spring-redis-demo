package com.purplevarun.springredisdemo.config;

import com.purplevarun.springredisdemo.service.CacheStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

/**
 * Wraps a delegate Cache to log HIT/MISS, track LRU order and enforce a max-size limit.
 */
public class LoggingCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(LoggingCache.class);

    private final Cache delegate;
    private final CacheStatsService statsService;
    private final int maxSize;
    // access-order=true: every put/get moves the entry to MRU position; first entry = LRU
    private final LinkedHashMap<Object, Boolean> lruTracker;

    public LoggingCache(Cache delegate, CacheStatsService statsService, int maxSize) {
        this.delegate = delegate;
        this.statsService = statsService;
        this.maxSize = maxSize;
        this.lruTracker = new LinkedHashMap<>(maxSize + 1, 0.75f, true);
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
            synchronized (lruTracker) { lruTracker.put(key, Boolean.TRUE); }
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
            synchronized (lruTracker) { lruTracker.put(key, Boolean.TRUE); }
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
        evictLruIfNeeded(key);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper result = delegate.putIfAbsent(key, value);
        if (result == null) {
            evictLruIfNeeded(key);
        }
        return result;
    }

    @Override
    public void evict(Object key) {
        synchronized (lruTracker) { lruTracker.remove(key); }
        delegate.evict(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        synchronized (lruTracker) { lruTracker.remove(key); }
        return delegate.evictIfPresent(key);
    }

    @Override
    public void clear() {
        synchronized (lruTracker) { lruTracker.clear(); }
        delegate.clear();
    }

    @Override
    public boolean invalidate() {
        synchronized (lruTracker) { lruTracker.clear(); }
        return delegate.invalidate();
    }

    private void evictLruIfNeeded(Object newKey) {
        synchronized (lruTracker) {
            lruTracker.put(newKey, Boolean.TRUE);
            if (lruTracker.size() > maxSize) {
                Object lruKey = lruTracker.keySet().iterator().next();
                lruTracker.remove(lruKey);
                delegate.evict(lruKey);
                log.info("LRU eviction: removed key={} (max size {} exceeded)", lruKey, maxSize);
                statsService.recordEviction();
            }
        }
    }
}

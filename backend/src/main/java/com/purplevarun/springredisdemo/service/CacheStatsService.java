package com.purplevarun.springredisdemo.service;

import com.purplevarun.springredisdemo.dto.CacheStatsResponse;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CacheStatsService {

    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    // thread-local so the controller can read HIT/MISS status for the current request
    private final ThreadLocal<Boolean> lastRequestHit = new ThreadLocal<>();

    public void recordHit() {
        hits.incrementAndGet();
        lastRequestHit.set(true);
    }

    public void recordMiss() {
        misses.incrementAndGet();
        lastRequestHit.set(false);
    }

    public String getLastCacheStatus() {
        Boolean hit = lastRequestHit.get();
        lastRequestHit.remove();
        if (hit == null) return "UNKNOWN";
        return hit ? "HIT" : "MISS";
    }

    public CacheStatsResponse toResponse() {
        long h = hits.get();
        long m = misses.get();
        long total = h + m;
        double hitRate = total == 0 ? 0.0 : (double) h / total;
        return new CacheStatsResponse(h, m, hitRate);
    }

    public void reset() {
        hits.set(0);
        misses.set(0);
        lastRequestHit.remove();
    }
}

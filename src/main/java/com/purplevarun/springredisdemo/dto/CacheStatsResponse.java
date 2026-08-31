package com.purplevarun.springredisdemo.dto;

public class CacheStatsResponse {

    private final long hits;
    private final long misses;
    private final double hitRate;

    public CacheStatsResponse(long hits, long misses, double hitRate) {
        this.hits = hits;
        this.misses = misses;
        this.hitRate = hitRate;
    }

    public long getHits() { return hits; }
    public long getMisses() { return misses; }
    public double getHitRate() { return hitRate; }
}

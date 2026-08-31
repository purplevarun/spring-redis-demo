package com.purplevarun.springredisdemo.dto;

public class CacheStatsResponse {

    private final long hits;
    private final long misses;
    private final long evictions;
    private final double hitRate;

    public CacheStatsResponse(long hits, long misses, long evictions, double hitRate) {
        this.hits = hits;
        this.misses = misses;
        this.evictions = evictions;
        this.hitRate = hitRate;
    }

    public long getHits() { return hits; }
    public long getMisses() { return misses; }
    public long getEvictions() { return evictions; }
    public double getHitRate() { return hitRate; }
}

package com.purplevarun.springredisdemo.controller;

import com.purplevarun.springredisdemo.dto.CacheStatsResponse;
import com.purplevarun.springredisdemo.service.CacheStatsService;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheStatsService statsService;
    private final CacheManager cacheManager;

    public CacheController(CacheStatsService statsService, CacheManager cacheManager) {
        this.statsService = statsService;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/stats")
    public CacheStatsResponse getStats() {
        return statsService.toResponse();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCache() {
        // evict the single 'all' entry rather than clearing the entire cache namespace
        cacheManager.getCache("numbers").evict("all");
        return ResponseEntity.noContent().build();
    }
}

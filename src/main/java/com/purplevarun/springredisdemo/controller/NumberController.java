package com.purplevarun.springredisdemo.controller;

import com.purplevarun.springredisdemo.dto.NumberRequest;
import com.purplevarun.springredisdemo.model.NumberEntry;
import com.purplevarun.springredisdemo.service.CacheStatsService;
import com.purplevarun.springredisdemo.service.NumberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/numbers")
public class NumberController {

    private final NumberService numberService;
    private final CacheStatsService cacheStatsService;

    public NumberController(NumberService numberService, CacheStatsService cacheStatsService) {
        this.numberService = numberService;
        this.cacheStatsService = cacheStatsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NumberEntry create(@Valid @RequestBody NumberRequest request) {
        return numberService.createNumber(request);
    }

    @GetMapping
    public List<NumberEntry> getAll(HttpServletResponse response) {
        List<NumberEntry> numbers = numberService.getAllNumbers();
        response.setHeader("X-Cache-Status", cacheStatsService.getLastCacheStatus());
        return numbers;
    }
}

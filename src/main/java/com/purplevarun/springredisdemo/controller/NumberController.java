package com.purplevarun.springredisdemo.controller;

import com.purplevarun.springredisdemo.dto.NumberRequest;
import com.purplevarun.springredisdemo.model.NumberEntry;
import com.purplevarun.springredisdemo.service.NumberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/numbers")
public class NumberController {

    private final NumberService numberService;

    public NumberController(NumberService numberService) {
        this.numberService = numberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NumberEntry create(@Valid @RequestBody NumberRequest request) {
        return numberService.createNumber(request);
    }

    @GetMapping
    public List<NumberEntry> getAll() {
        return numberService.getAllNumbers();
    }
}

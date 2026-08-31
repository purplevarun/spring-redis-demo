package com.purplevarun.springredisdemo.service;

import com.purplevarun.springredisdemo.dto.NumberRequest;
import com.purplevarun.springredisdemo.model.NumberEntry;
import com.purplevarun.springredisdemo.repository.NumberRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NumberService {

    private final NumberRepository repository;

    public NumberService(NumberRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = "numbers", key = "'all'")
    public List<NumberEntry> getAllNumbers() {
        return repository.findAll();
    }

    @CacheEvict(cacheNames = "numbers", key = "'all'")
    public NumberEntry createNumber(NumberRequest request) {
        NumberEntry entry = new NumberEntry();
        entry.setValue(request.getValue());
        return repository.save(entry);
    }
}

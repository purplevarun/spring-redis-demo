package com.purplevarun.springredisdemo.service;

import com.purplevarun.springredisdemo.dto.NumberRequest;
import com.purplevarun.springredisdemo.model.NumberEntry;
import com.purplevarun.springredisdemo.repository.NumberRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class NumberService {

    private final NumberRepository repository;

    public NumberService(NumberRepository repository) {
        this.repository = repository;
    }

    public List<NumberEntry> getAllNumbers() {
        return repository.findAll();
    }

    public NumberEntry createNumber(NumberRequest request) {
        NumberEntry entry = new NumberEntry();
        entry.setValue(request.getValue());
        return repository.save(entry);
    }

    // cached per entry; LRU eviction is enforced by LoggingCache when size > app.cache.max-size
    @Cacheable(cacheNames = "numbers", key = "#id")
    public NumberEntry getNumberById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Number not found: " + id));
    }
}

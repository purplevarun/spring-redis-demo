package com.purplevarun.springredisdemo.service;

import com.purplevarun.springredisdemo.dto.NumberRequest;
import com.purplevarun.springredisdemo.model.NumberEntry;
import com.purplevarun.springredisdemo.repository.NumberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NumberServiceTest {

    @Mock
    private NumberRepository repository;

    @InjectMocks
    private NumberService service;

    private NumberEntry entry;

    @BeforeEach
    void setUp() {
        entry = new NumberEntry(UUID.randomUUID(), 42, Instant.now());
    }

    @Test
    void getAllNumbers_returnsAllEntriesFromRepository() {
        when(repository.findAll()).thenReturn(List.of(entry));

        List<NumberEntry> result = service.getAllNumbers();

        assertThat(result).containsExactly(entry);
        verify(repository).findAll();
    }

    @Test
    void getAllNumbers_returnsEmptyListWhenRepositoryIsEmpty() {
        when(repository.findAll()).thenReturn(List.of());

        List<NumberEntry> result = service.getAllNumbers();

        assertThat(result).isEmpty();
    }

    @Test
    void createNumber_persistsEntryWithCorrectValue() {
        NumberRequest request = new NumberRequest(99);
        when(repository.save(any(NumberEntry.class))).thenReturn(entry);

        NumberEntry result = service.createNumber(request);

        assertThat(result).isEqualTo(entry);
        ArgumentCaptor<NumberEntry> captor = ArgumentCaptor.forClass(NumberEntry.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getValue()).isEqualTo(99);
    }
}

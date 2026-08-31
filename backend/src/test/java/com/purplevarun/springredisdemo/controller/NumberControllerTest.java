package com.purplevarun.springredisdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.purplevarun.springredisdemo.dto.NumberRequest;
import com.purplevarun.springredisdemo.model.NumberEntry;
import com.purplevarun.springredisdemo.service.CacheStatsService;
import com.purplevarun.springredisdemo.service.NumberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NumberController.class)
class NumberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NumberService numberService;

    @MockBean
    private CacheStatsService cacheStatsService;

    @Test
    void post_validRequest_returns201WithCreatedEntry() throws Exception {
        NumberEntry saved = new NumberEntry(UUID.randomUUID(), 7, Instant.now());
        when(numberService.createNumber(any())).thenReturn(saved);

        mockMvc.perform(post("/api/numbers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NumberRequest(7))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value").value(7));
    }

    @Test
    void post_missingValue_returns400() throws Exception {
        mockMvc.perform(post("/api/numbers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_returnsAllNumbers_200() throws Exception {
        NumberEntry entry = new NumberEntry(UUID.randomUUID(), 42, Instant.now());
        when(numberService.getAllNumbers()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/numbers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value(42));
    }

    @Test
    void getById_returns200WithCacheStatusHeader() throws Exception {
        NumberEntry entry = new NumberEntry(UUID.randomUUID(), 7, Instant.now());
        when(numberService.getNumberById(entry.getId())).thenReturn(entry);
        when(cacheStatsService.getLastCacheStatus()).thenReturn("MISS");

        mockMvc.perform(get("/api/numbers/" + entry.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(7))
                .andExpect(header().string("X-Cache-Status", "MISS"));
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(numberService.getNumberById(id)).thenThrow(new java.util.NoSuchElementException());

        mockMvc.perform(get("/api/numbers/" + id))
                .andExpect(status().isNotFound());
    }
}

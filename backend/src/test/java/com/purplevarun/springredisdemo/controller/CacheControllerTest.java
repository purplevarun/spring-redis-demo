package com.purplevarun.springredisdemo.controller;

import com.purplevarun.springredisdemo.dto.CacheStatsResponse;
import com.purplevarun.springredisdemo.service.CacheStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CacheController.class)
class CacheControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CacheStatsService statsService;

    @MockBean
    private CacheManager cacheManager;

    @Test
    void getStats_returnsStatsFromService() throws Exception {
        when(statsService.toResponse()).thenReturn(new CacheStatsResponse(5, 3, 1, 0.625));

        mockMvc.perform(get("/api/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits").value(5))
                .andExpect(jsonPath("$.misses").value(3))
                .andExpect(jsonPath("$.hitRate").value(0.625));
    }

    @Test
    void clearCache_evictsEntryAndReturns204() throws Exception {
        org.springframework.cache.Cache mockCache = mock(org.springframework.cache.Cache.class);
        when(cacheManager.getCache("numbers")).thenReturn(mockCache);

        mockMvc.perform(delete("/api/cache"))
                .andExpect(status().isNoContent());

        verify(mockCache).evict("all");
    }
}

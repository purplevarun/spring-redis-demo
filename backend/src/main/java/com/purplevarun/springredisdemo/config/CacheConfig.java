package com.purplevarun.springredisdemo.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.purplevarun.springredisdemo.service.CacheStatsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.max-size:3}")
    private int maxSize;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     CacheStatsService statsService) {
        // JavaTimeModule required to serialize Instant; default typing embeds @class for polymorphic deserialization
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build(),
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY)
                .build();

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        CacheManager delegate = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();

        // same LoggingCache instance must be returned for the same name so the lruTracker persists across calls
        ConcurrentHashMap<String, Cache> cacheInstances = new ConcurrentHashMap<>();

        return new CacheManager() {
            @Override
            public Cache getCache(String name) {
                return cacheInstances.computeIfAbsent(name, n -> {
                    Cache cache = delegate.getCache(n);
                    return cache == null ? null : new LoggingCache(cache, statsService, maxSize);
                });
            }

            @Override
            public Collection<String> getCacheNames() {
                return delegate.getCacheNames();
            }
        };
    }
}

package com.purplevarun.springredisdemo.config;

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

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.max-size:3}")
    private int maxSize;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     CacheStatsService statsService) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        CacheManager delegate = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();

        // wrap every cache instance so all get() calls are logged, counted and size-limited
        return new CacheManager() {
            @Override
            public Cache getCache(String name) {
                Cache cache = delegate.getCache(name);
                return cache == null ? null : new LoggingCache(cache, statsService, maxSize);
            }

            @Override
            public Collection<String> getCacheNames() {
                return delegate.getCacheNames();
            }
        };
    }
}

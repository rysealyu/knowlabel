package com.knowlabel.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * CacheConfig
 */
@Configuration 
@EnableCaching 
public class CacheConfig {

    @Bean 
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // 1. Individual Ingredients Cache
        // - 10,000 max items with an expiration date of 7 days (1 week)
        CaffeineCache ingredientCache = new CaffeineCache("ingredients", 
                Caffeine.newBuilder()
                        .maximumSize(10_1000)
                        .expireAfterWrite(7, TimeUnit.DAYS)
                        .recordStats()
                        .build()
        );

        // 2. Overall Product Analysis Cache
        // - 5,000 max items (to protect JVM memory due to heavy JSON payloads) with an expiration date of 24 hours (1 day)
        CaffeineCache productAnalysisCache = new CaffeineCache("productAnalysis", 
                Caffeine.newBuilder()
                        .maximumSize(5_000)
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .recordStats()
                        .build()
        );

        cacheManager.setCaches(List.of(ingredientCache, productAnalysisCache));
        return cacheManager;
    }
}
package com.knowlabel.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Configuration class for the cache.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    /**
     * Maximum number of ingredients to cache.
     */
    private static final int MAX_INGREDIENTS_CACHE_SIZE = 10_000;

    /**
     * Maximum number of product analyses to cache.
     */
    private static final int MAX_PRODUCT_ANALYSIS_CACHE_SIZE = 5_000;

    /**
     * Expiration time for ingredient cache entries, in days.
     */
    private static final int INGREDIENT_CACHE_EXPIRATION_DAYS = 7;

    /**
     * Expiration time for product analysis cache entries, in hours.
     */
    private static final int PRODUCT_ANALYSIS_CACHE_EXPIRATION_HOURS = 24;

    /**
     * Creates and configures the application's cache manager.
     *
     * @return the configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        CaffeineCache ingredientCache = new CaffeineCache("ingredients",
                Caffeine.newBuilder()
                        .maximumSize(MAX_INGREDIENTS_CACHE_SIZE)
                        .expireAfterWrite(
                            INGREDIENT_CACHE_EXPIRATION_DAYS,
                            TimeUnit.DAYS
                        )
                        .recordStats()
                        .build()
        );

        CaffeineCache analysisCache = new CaffeineCache("ingredientsAnalysis",
                Caffeine.newBuilder()
                        .maximumSize(MAX_PRODUCT_ANALYSIS_CACHE_SIZE)
                        .expireAfterWrite(
                            PRODUCT_ANALYSIS_CACHE_EXPIRATION_HOURS,
                            TimeUnit.HOURS
                        )
                        .recordStats()
                        .build()
        );

        cacheManager.setCaches(List.of(ingredientCache, analysisCache));
        return cacheManager;
    }
}

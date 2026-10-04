package com.knowlabel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

import com.knowlabel.config.CacheConfig;
import com.knowlabel.model.IngredientAnalysisModel;
import com.knowlabel.model.IngredientModel;

import reactor.core.publisher.Mono;

/**
 * IngredientServiceCacheTest
 */
@SpringBootTest(classes = {CacheConfig.class, IngredientService.class})
class IngredientServiceCacheTest {

    @Autowired 
    private IngredientService ingredientService;

    @Autowired 
    private CacheManager cacheManager;

    @MockitoBean(name = "inciWebClient", answers = Answers.RETURNS_DEEP_STUBS)
    private WebClient inciWebClient;
    
    @BeforeEach 
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache =  cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test 
    @DisplayName("Caches bulk analysis on first call and harvests individual ingredients")
    void analyzeIngredients_CachesBulkAndHarvestsIndividuals() {
        IngredientModel niacinamide = new IngredientModel(
        "NIACINAMIDE", 9, "safe", false, List.of(), 0, "none", true, true, true
        );
        IngredientAnalysisModel.AnalysisDetails details = new IngredientAnalysisModel.AnalysisDetails(
                null, List.of("NIACINAMIDE"), List.of(niacinamide),
                9.0, "low_risk", List.of(), Map.of("oily", "good"),
                true, List.of(), 1, 0.0, List.of(), 1.0, Map.of(), "2026-10-03T12:00:00Z"
        );
        IngredientAnalysisModel mockApiResponse = new IngredientAnalysisModel(details);

        when(inciWebClient.post()
                .uri(anyString())
                .bodyValue(any())
                .retrieve()
                .bodyToMono(IngredientAnalysisModel.class))
                .thenReturn(Mono.just(mockApiResponse));
        
        clearInvocations(inciWebClient);

        List<String> input = List.of("NIACINAMIDE");
        String cacheKey = "NIACINAMIDE";

        // Cache is empty -> Should hit the WebClient and populate the cache
        IngredientAnalysisModel firstCall = ingredientService.analyzeIngredients(input, cacheKey);
        // Cache is warm -> Should return instantly from the Caffine cache layer
        IngredientAnalysisModel secondCall = ingredientService.analyzeIngredients(input, cacheKey);

        // Make sure both calls from Cache and INCI API are the exactly the same
        assertThat(firstCall).isEqualTo(secondCall);

        // Prove that the 1st Tier cache worked by checking if inciWebClient was only triggered once (since the Cache skips the WebClient)
        verify(inciWebClient, times(1)).post();

        Cache individualCache = cacheManager.getCache("ingredients");
        assertThat(individualCache).isNotNull();

        // Make sure that ingredient is cached in the "ingredients" group
        IngredientModel cachedIngredient = individualCache.get("NIACINAMIDE", IngredientModel.class);
        assertThat(cachedIngredient).isNotNull();
        assertThat(cachedIngredient.safetyScore()).isEqualTo(9);
    }
}
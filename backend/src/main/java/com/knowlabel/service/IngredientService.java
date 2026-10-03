package com.knowlabel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.knowlabel.model.IngredientAnalysisModel;
import com.knowlabel.model.IngredientModel;

/**
 * IngredientService
 */
@Service
public class IngredientService {
    private final WebClient inciWebClient;
    private final CacheManager cacheManager;

    public IngredientService(@Qualifier("inciWebClient") WebClient inciWebClient, CacheManager cacheManager) {
        this.inciWebClient = inciWebClient;
        this.cacheManager = cacheManager;
    }

    @Cacheable(value = "ingredients", key = "#inciName.toUpperCase()")
    public IngredientModel fetchSingleIngredient(String inciName) {
        return inciWebClient.get()
            .uri("/ingredients/{inciName}", inciName)
            .retrieve()
            .bodyToMono(IngredientModel.class)
            .block();
    }
    
    @Cacheable(value = "ingredientsAnalysis", key = "#key")
    public IngredientAnalysisModel analyzeIngredients(List<String> ingredients, String key) {
        Map<String, Object> body = new HashMap<>();
        body.put("inci", ingredients);

        IngredientAnalysisModel response = inciWebClient.post()
            .uri("/analyze")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(IngredientAnalysisModel.class)
            .block();
        
        // * For future use case if we want to use individual ingredients from the in-memory cache
        if (response != null && response.analysis() != null) {
            List<IngredientModel> parsedList = response.analysis().parsedIngredients();

            if (parsedList != null && !parsedList.isEmpty()) {
                var individualCache = cacheManager.getCache("ingredients");

                if (individualCache != null) {
                    for (IngredientModel ingredient : parsedList) {
                        if (ingredient.inciName() != null && !ingredient.inciName().isEmpty()) {
                            individualCache.put(ingredient.inciName().toUpperCase(), ingredient);
                        }
                    }
                }
            }
        }

        return response;
    }
}
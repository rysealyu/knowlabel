package com.knowlabel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.knowlabel.model.IngredientAnalysisModel;
import com.knowlabel.model.IngredientModel;

/**
 * Provides ingredient lookup and ingredient analysis operations.
 */
@Service
public class IngredientService {
    /**
     * Client used to communicate with the INCI analysis API.
     */
    private final WebClient inciWebClient;

    /**
     * Cache manager used to populate the individual ingredient cache.
     */
    private final CacheManager cacheManager;

    /**
     * Creates an IngredientService.
     *
     * @param webClient the client used to call the INCI analysis API
     * @param manager the cache manager used by the service
     */
    public IngredientService(
            @Qualifier("inciWebClient") final WebClient webClient,
            final CacheManager manager) {
        this.inciWebClient = webClient;
        this.cacheManager = manager;
    }

    /**
     * Fetches details for one ingredient.
     *
     * <p>Subclasses may override this method when extending the service,
     * provided they preserve the ingredient-cache contract.</p>
     *
     * @param inciName the ingredient name to fetch
     * @return the ingredient details returned by the INCI API
     */
    @Cacheable(value = "ingredients", key = "#inciName.toUpperCase()")
    public IngredientModel fetchSingleIngredient(final String inciName) {
        return inciWebClient.get()
            .uri("/ingredients/{inciName}", inciName)
            .retrieve()
            .bodyToMono(IngredientModel.class)
            .block();
    }

    /**
     * Analyzes a list of ingredients.
     *
     * <p>Subclasses may override this method when extending the service,
     * provided they preserve the analysis-cache contract.</p>
     *
     * @param ingredients the ingredients to analyze
     * @param key the cache key for the analysis request
     * @return the ingredient analysis returned by the INCI API
     */
    @Cacheable(value = "ingredientsAnalysis", key = "#key")
    public IngredientAnalysisModel analyzeIngredients(
            final List<String> ingredients,
            final String key) {
        final Map<String, Object> body = new HashMap<>();
        body.put("inci", ingredients);

        final IngredientAnalysisModel response = inciWebClient.post()
            .uri("/analyze")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(IngredientAnalysisModel.class)
            .block();

        if (response != null && response.analysis() != null) {
            final List<IngredientModel> parsedList =
                response.analysis().parsedIngredients();

            if (CollectionUtils.isNotEmpty(parsedList)) {
                final var individualCache =
                    cacheManager.getCache("ingredients");

                if (individualCache != null) {
                    for (final IngredientModel i : parsedList) {
                        boolean isIngredientValid =
                            StringUtils.hasText(i.inciName());

                        if (isIngredientValid) {
                            individualCache.put(
                                i.inciName().toUpperCase(),
                                i
                            );
                        }
                    }
                }
            }
        }

        return response;
    }
}

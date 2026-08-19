package com.knowlabel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.JsonNode; 

/**
 * IngredientService
 */
@Service
public class IngredientService {
    private final WebClient inciWebClient;

    public IngredientService(@Qualifier("inciWebClient") WebClient inciWebClient) {
        this.inciWebClient = inciWebClient;
    }

    public JsonNode anaylizeIngredients(List<String> ingredients) {
        Map<String, Object> body = new HashMap<>();
        body.put("inci", ingredients);

        return inciWebClient.post()
            .uri("/analyze")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();
    }
    
}
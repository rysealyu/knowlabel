package com.knowlabel.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.knowlabel.model.IngredientAnalysisModel;
import com.knowlabel.service.IngredientService;

/**
 * ProductController
 */
@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {
    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeIngredients(@RequestBody List<String> ingredients) {
        try {
            if (ingredients == null) {
                ingredients = List.of();
            }

            List<String> sortedIngredients = new ArrayList<>();
            for (String item : ingredients) {
                if (item != null) {
                    sortedIngredients.add(item.trim().toUpperCase());
                }
            }

            Collections.sort(sortedIngredients);
            String key = String.join(",", sortedIngredients);

            // Call the service
            IngredientAnalysisModel result = ingredientService.analyzeIngredients(sortedIngredients, key);

            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body("Third-party API error: " + e.getStatusText());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occured: " + e.getMessage());
        }
    }
}
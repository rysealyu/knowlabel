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
 * Ingreident Controller handles HTTP requests related to ingredient analysis.
 */
@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {
    /**
     * The IngredientService instance used to perform ingredient analysis.
     */
    private final IngredientService ingredientService;

    /**
     * Constructs an IngredientController with the specified IngredientService.
     * @param service
     */
    public IngredientController(final IngredientService service) {
        this.ingredientService = service;
    }

    /**
     * Analyzes the provided list of ingredients.
     * @param ingredients the list of ingredients to analyze
     * @return ResponseEntity that is the analysis results or an error message
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeIngredients(
            @RequestBody final List<String> ingredients) {
        try {
            final List<String> inputIngredients = ingredients == null
                    ? List.of()
                    : ingredients;

            final List<String> sortedIngredients = new ArrayList<>();
            for (final String item : inputIngredients) {
                if (item != null) {
                    sortedIngredients.add(item.trim().toUpperCase());
                }
            }

            Collections.sort(sortedIngredients);
            final String key = String.join(",", sortedIngredients);

            final IngredientAnalysisModel result =
                    ingredientService.analyzeIngredients(
                        sortedIngredients, key);

            return ResponseEntity.ok(result);
        } catch (WebClientResponseException exception) {
            return ResponseEntity
                    .status(exception.getStatusCode())
                    .body(
                        "Third-party API error: "
                            + exception.getStatusText());
        } catch (Exception exception) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: "
                            + exception.getMessage());
        }
    }
}

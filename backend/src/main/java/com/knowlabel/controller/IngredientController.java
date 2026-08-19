package com.knowlabel.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.JsonNode; 

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
    public JsonNode anaylizeIngredients(@RequestBody List<String> ingredients) {
        return ingredientService.anaylizeIngredients(ingredients);
    }
    
}
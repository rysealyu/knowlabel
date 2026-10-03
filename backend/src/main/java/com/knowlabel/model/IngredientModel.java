package com.knowlabel.model;

import java.util.List;
/**
 * Ingredient
 */
public record IngredientModel(
    String inciName,
    int safetyScore,
    String safetyLevel,
    boolean isAllergen,
    List<String> allergenTypes,
    Integer comedogenicityRating, // Wrapper class because it can be null (e.g., Linalool)
    String irritancyPotential,
    Boolean pregnancySafe,        // Wrapper class because it can be null
    boolean found,
    boolean hasData
) {} 
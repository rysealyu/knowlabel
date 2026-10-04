package com.knowlabel.model;

import java.util.List;

/**
 * IngredientModel.
 *
 * @param inciName the INCI name of the ingredient
 * @param safetyScore the safety score of the ingredient
 * @param safetyLevel the safety level of the ingredient
 * @param isAllergen whether the ingredient is an allergen
 * @param allergenTypes the types of allergens associated with the ingredient
 * @param comedogenicityRating the comedogenicity rating of the ingredient
 * @param irritancyPotential the irritancy potential of the ingredient
 * @param pregnancySafe whether the ingredient is safe for use during pregnancy
 * @param found whether the ingredient was found in the analysis
 * @param hasData whether there is data available for the ingredient
 */
public record IngredientModel(
    String inciName,
    int safetyScore,
    String safetyLevel,
    boolean isAllergen,
    List<String> allergenTypes,
    Integer comedogenicityRating,
    String irritancyPotential,
    Boolean pregnancySafe,
    boolean found,
    boolean hasData
) { }

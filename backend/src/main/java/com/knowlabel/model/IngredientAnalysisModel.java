package com.knowlabel.model;

import java.util.List;
import java.util.Map;

/**
 * IngredientAnalysisModel
 */
public record IngredientAnalysisModel(
    AnalysisDetails analysis
) {
    public record AnalysisDetails(
                String barcode,
                List<String> rawInci,
                List<IngredientModel> parsedIngredients,
                Double overallSafetyScore,
                String safetyLevel,
                List<String> allergenFlags,
                Map<String, String> skinTypeCompatibility,
                Boolean pregnancySafe,
                List<String> pregnancyUnsafeIngredients,
                Integer cleanBeautyScore,
                Double comedogenicityScore,
                List<String> pfasIngredients,
                Double coverage,
                Map<String, Boolean> flags,
                String analyzedAt
    ) {}
}
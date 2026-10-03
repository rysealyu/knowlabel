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
                double overallSafetyScore,
                String safetyLevel,
                List<String> allergenFlags,
                Map<String, String> skinTypeCompatibility,
                boolean pregnancySafe,
                List<String> pregnancyUnsafeIngredients,
                int cleanBeautyScore,
                double comedogenicityScore,
                List<String> pfasIngredients,
                double coverage,
                Map<String, Boolean> flags,
                String analyzedAt
    ) {}
}
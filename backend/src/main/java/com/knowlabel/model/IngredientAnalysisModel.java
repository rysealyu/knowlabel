package com.knowlabel.model;

import java.util.List;
import java.util.Map;

/**
 * IngredientAnalysisModel.
 *
 * @param analysis the analysis details of the ingredient analysis
 */
public record IngredientAnalysisModel(
    AnalysisDetails analysis
) {
    /**
     * AnalysisDetails record represents the details of an ingredient analysis.
     *
     * @param barcode the unique product identifier or barcode scanned
     * @param rawInci the raw International Nomenclature of Cosmetic
     *        Ingredients (INCI) text entries
     * @param parsedIngredients the list of structured ingredient models
     *        extracted from the raw input
     * @param overallSafetyScore the calculated overall product safety score
     * @param safetyLevel a descriptive rating representing product safety
     *        (e.g., Low, Moderate, High Hazard)
     * @param allergenFlags identified allergen substances present in the
     *        formulation
     * @param skinTypeCompatibility mapping of target skin types to their
     *        respective suitability ratings
     * @param pregnancySafe {@code true} if the formulation contains no known
     *        pregnancy contraindications; {@code false} otherwise
     * @param pregnancyUnsafeIngredients names of ingredients identified as
     *        unsafe for use during pregnancy
     * @param cleanBeautyScore the compliance score according to clean beauty
     *        standards
     * @param comedogenicityScore the aggregate score indicating pore-clogging
     *        potential
     * @param pfasIngredients identified per- and polyfluoroalkyl substances
     *        found in the formulation
     * @param coverage the percentage or proportion of ingredients recognized
     *        and evaluated by the analysis engine
     * @param flags general Boolean diagnostic and warning indicators
     * @param analyzedAt the timestamp or ISO-8601 string when the analysis
     *        was executed
     */
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
    ) { }
}

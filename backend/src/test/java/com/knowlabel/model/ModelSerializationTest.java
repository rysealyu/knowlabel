package com.knowlabel.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

/**
 * ModelSerializationTest
 */
@JsonTest 
class ModelSerializationTest {

    @Autowired 
    private JacksonTester<IngredientAnalysisModel> jsonTester;

    @Test 
    @DisplayName("Deserializes JSON with nullable fields into records without crashing")
    void deserialize_HandlesNullFieldsGracefully() throws Exception {
        String rawJson = """
                {
                  "analysis": {
                    "barcode": null,
                    "rawInci": ["PARFUM"],
                    "parsedIngredients": [
                      {
                        "inciName": "PARFUM",
                        "safetyScore": 4,
                        "safetyLevel": "moderate_risk",
                        "isAllergen": true,
                        "allergenTypes": ["fragrance"],
                        "comedogenicityRating": null,
                        "irritancyPotential": "high",
                        "pregnancySafe": null,
                        "found": true,
                        "hasData": true
                      }
                    ],
                    "overallSafetyScore": 4.0,
                    "safetyLevel": "moderate_risk"
                  }
                }
                """;
        
        IngredientAnalysisModel parsed = jsonTester.parseObject(rawJson);

        assertThat(parsed.analysis()).isNotNull();
        assertThat(parsed.analysis().barcode()).isNull();
        assertThat(parsed.analysis().overallSafetyScore()).isEqualTo(4.0);

        IngredientModel firstIngredient = parsed.analysis().parsedIngredients().get(0);
        assertThat(firstIngredient.inciName()).isEqualTo("PARFUM");
        assertThat(firstIngredient.safetyScore()).isEqualTo(4);
        assertThat(firstIngredient.isAllergen()).isTrue();
        assertThat(firstIngredient.comedogenicityRating()).isNull();
        assertThat(firstIngredient.pregnancySafe()).isNull();
    }
    
}
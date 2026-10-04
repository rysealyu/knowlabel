package com.knowlabel.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.knowlabel.model.IngredientAnalysisModel;
import com.knowlabel.service.IngredientService;

/**
 * IngredientControllerTest
 */
@WebMvcTest(IngredientController.class)
class IngredientControllerTest {

    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean 
    private IngredientService ingredientService;

    @Test 
    @DisplayName("POST /analyze normalizes, sorts, and builds a deterministic cache key")
    void analyzeIngredients_NormalizesAndSortsPayload() throws Exception {
        String rawJsonPayload = "[\" niacinamide \", null, \"water\", \"GLYCERIN\"]";
        List<String> expectedSortedList = List.of("GLYCERIN", "NIACINAMIDE", "WATER");
        String expectedKey = "GLYCERIN,NIACINAMIDE,WATER";

        IngredientAnalysisModel mockResponse = new IngredientAnalysisModel(null);

        when(ingredientService.analyzeIngredients(eq(expectedSortedList), eq(expectedKey)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/ingredients/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rawJsonPayload))
                    .andExpect(status().isOk());

        verify(ingredientService).analyzeIngredients(eq(expectedSortedList), eq(expectedKey));
    }

    @Test 
    @DisplayName("POST /analyze translates upstream WebClientResponseException into 502 Bad Gateway")
    void analyzeIngredients_HandlesUpsreamApiError() throws Exception {
        String rawJsonPayload = "[\"WATER\"]";
        List<String> expectedList = List.of("WATER");
        String expectedKey = "WATER";

        // Fake 502 exception and communicate with the mock service to throw it
        WebClientResponseException upstreamException = WebClientResponseException.create(
            HttpStatus.BAD_GATEWAY.value(), 
            "Bad Gateway", 
            HttpHeaders.EMPTY, 
            new byte[0], 
            null
        );

        when(ingredientService.analyzeIngredients(eq(expectedList), eq(expectedKey)))
                .thenThrow(upstreamException);

        mockMvc.perform(post("/api/ingredients/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rawJsonPayload))
                    .andExpect(status().isBadGateway());
    }
}
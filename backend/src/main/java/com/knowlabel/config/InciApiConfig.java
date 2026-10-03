
package com.knowlabel.config;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.models.Secret;
import com.infisical.sdk.util.InfisicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class InciApiConfig {
    private final String apiKey;

    public InciApiConfig(
        InfisicalSdk sdk, 
        @Value("${infisical.project-id}") String projectId
    ) {
        try {
            Secret secret = sdk.Secrets().GetSecret(
                "INCI_API_KEY",
                projectId,
                "dev",
                "/",
                null,
                null,
                null
            );
            this.apiKey = secret.getSecretValue();
        } catch (InfisicalException e) {
            throw new RuntimeException("Failed to fetch INCI_API_KEY from Infisical" + e.getMessage());
        }
    }
    
    @Bean("inciWebClient")
    public WebClient inciWebClient() {
        return WebClient.builder()
            .baseUrl("https://inciapi.com/v1")
            .defaultHeader("X-API-Key", apiKey)
            .build();
    }   
}
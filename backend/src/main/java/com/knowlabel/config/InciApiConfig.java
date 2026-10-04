package com.knowlabel.config;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.models.Secret;
import com.infisical.sdk.util.InfisicalException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for the INCI API.
 */
@Configuration
public class InciApiConfig {
    /**
     * The API key for accessing the INCI API, fetched securely from Infisical.
     */
    private final String apiKey;

    /**
     * Constructs InciApiConfig instance, fetches INCI API key from Infisical.
     *
     * @param sdk the Infisical SDK used to fetch secrets
     * @param projectId the Infisical project ID
     */
    public InciApiConfig(
        final InfisicalSdk sdk,
        @Value("${infisical.project-id}") final String projectId
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
            throw new RuntimeException(
                "Failed to fetch INCI_API_KEY from Infisical" + e.getMessage()
            );
        }
    }

    /**
     * Creates and configures a WebClient for interacting with the INCI API.
     *
     * @return a configured WebClient instance
     */
    @Bean("inciWebClient")
    public WebClient inciWebClient() {
        return WebClient.builder()
            .baseUrl("https://inciapi.com/v1")
            .defaultHeader("X-API-Key", apiKey)
            .build();
    }
}

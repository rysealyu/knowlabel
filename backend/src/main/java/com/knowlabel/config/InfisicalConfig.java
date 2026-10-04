package com.knowlabel.config;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.config.SdkConfig;
import com.infisical.sdk.util.InfisicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Infisical SDK integration.
 */
@Configuration
public class InfisicalConfig {
    /**
     * The client ID for the Infisical application.
     */
    @Value("${infisical.client-id}")
    private String infisicalClientId;

    /**
     * The client secret for the Infisical application.
     */
    @Value("${infisical.client-secret}")
    private String infisicalClientSecret;

    /**
     * Constructs an InfisicalSdk instance and ...
     * authenticates it using the provided client ID and secret.
     *
     * @return a configured InfisicalSdk instance
     */
    @Bean
    public InfisicalSdk infisicalSdk() {
        InfisicalSdk sdk = new InfisicalSdk(
            new SdkConfig.Builder()
                .withSiteUrl("https://app.infisical.com")
                .build()
        );

        try {
            sdk.Auth().UniversalAuthLogin(
                infisicalClientId,
                infisicalClientSecret
            );
        } catch (InfisicalException e) {
            throw new RuntimeException(
                "Failed to authenticate with Infisical: " + e.getMessage()
            );
        }

        return sdk;
    }
}

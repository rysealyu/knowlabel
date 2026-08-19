package com.knowlabel.config;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.config.SdkConfig;
import com.infisical.sdk.util.InfisicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfisicalConfig {
    @Value("${infisical.client-id}")
    private String infisicalClientId;

    @Value("${infisical.client-secret}")
    private String infisicalClientSecret;

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
            throw new RuntimeException("Failed to authenticate with Infisical: " + e.getMessage());
        }

        return sdk;
    }
}

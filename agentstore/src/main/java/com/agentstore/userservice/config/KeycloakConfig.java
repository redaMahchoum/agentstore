package com.agentstore.userservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KeycloakConfig {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Bean
    public Keycloak keycloak() {
        try {
            return KeycloakBuilder.builder()
                    .serverUrl(authServerUrl)
                    .realm("master")  // The realm to use for the admin CLI
                    .clientId("admin-cli")
                    .username("admin")
                    .password("admin")
                    .build();
        } catch (Exception e) {
            // Log detailed error information
            log.error("Error initializing Keycloak client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Keycloak client: " + e.getMessage(), e);
        }
    }
} 
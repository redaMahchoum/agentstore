package com.agentstore.userservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.Arrays;
import java.util.List;

/**
 * Configures flexible JWT validation to accommodate different environments.
 * This helps when your application is accessed from different environments (local, Docker).
 */
@Configuration
public class JwtConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(JwtConfiguration.class);
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;
    
    @Value("${keycloak.auth-server-url:http://localhost:8081}")
    private String keycloakUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    /**
     * Custom JWT Decoder that accepts tokens from multiple allowed issuers.
     * This helps when tokens are obtained from Keycloak in different environments
     * (e.g., through Docker networking vs. direct browser access).
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        // Define acceptable issuers
        List<String> acceptableIssuers = Arrays.asList(
            String.format("%s/realms/%s", keycloakUrl, realm),
            String.format("http://localhost:8081/realms/%s", realm),
            String.format("http://keycloak:8080/realms/%s", realm)
        );
        
        // Create a custom validator that accepts multiple issuers
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> customValidator = new DelegatingOAuth2TokenValidator<>(
            defaultValidator,
            token -> {
                String issuer = token.getIssuer().toString();
                boolean valid = acceptableIssuers.stream().anyMatch(issuer::equals);
                
                if (valid) {
                    log.debug("JWT issuer is valid: {}", issuer);
                    return OAuth2TokenValidatorResult.success();
                } else {
                    log.debug("JWT issuer is invalid: {}. Acceptable issuers are: {}", issuer, acceptableIssuers);
                    return OAuth2TokenValidatorResult.failure(new org.springframework.security.oauth2.core.OAuth2Error("invalid_issuer", 
                            "Invalid issuer: " + issuer, null));
                }
            }
        );
        
        jwtDecoder.setJwtValidator(customValidator);
        return jwtDecoder;
    }
} 
package com.agentstore.userservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class AuthenticationLogger {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationLogger.class);

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        // Check if the successful authentication was based on a JWT
        if (event.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
            String principalName = jwtAuth.getName(); // Get the principal name (e.g., subject claim)
            log.debug("JWT Authentication SUCCESS for principal: '{}'. Token validated successfully.", principalName);
            
            // Log the authorities/roles that have been extracted
            log.debug("Granted authorities: {}", jwtAuth.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.joining(", ")));
            
            // Log the JWT claims for debugging
            if (jwtAuth.getToken() != null) {
                log.debug("JWT claims: {}", jwtAuth.getToken().getClaims());
            }
        } else {
             log.debug("Authentication SUCCESS (Type: {}), Principal: '{}'", event.getAuthentication().getClass().getSimpleName(), event.getAuthentication().getName());
        }
    }

    @EventListener
    public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        // Log details about the failure, including the exception message which often contains the reason
        String principalName = (event.getAuthentication() != null) ? event.getAuthentication().getName() : "Unknown";
        String exceptionMessage = (event.getException() != null) ? event.getException().getMessage() : "No exception details";
        
        // Check if it was likely a JWT attempt that failed
        // Note: The authentication object might be partially populated or null in failure cases
        log.debug("Authentication FAILURE for principal: '{}'. Reason: {}", principalName, exceptionMessage);

        // For more detailed JWT failure reasons, the underlying exception often needs inspection
        // The DEBUG logs for org.springframework.security.oauth2.server.resource should contain specifics
        if (log.isTraceEnabled() && event.getException() != null) {
            // Log stack trace only if TRACE is enabled, as it can be very verbose
             log.trace("Authentication failure stack trace:", event.getException());
        }
    }
} 
package com.agentstore.userservice.controller;

import com.agentstore.userservice.model.dto.UserDTO;
import com.agentstore.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Registers a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data")
    })
    public ResponseEntity<UserDTO> registerUser(
            @Parameter(description = "User registration data", required = true)
            @Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(userDTO));
    }
    
    @GetMapping("/token-debug")
    @Operation(summary = "Debug JWT token", description = "Returns information about the current user's JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token information retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Map<String, Object>> debugToken(Authentication authentication) {
        Map<String, Object> tokenInfo = new HashMap<>();
        
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            // Add JWT claims
            tokenInfo.put("claims", jwtAuth.getToken().getClaims());
            
            // Add extracted authorities/roles
            tokenInfo.put("authorities", jwtAuth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
                    
            // Add principal name
            tokenInfo.put("principal", jwtAuth.getName());
        } else {
            tokenInfo.put("message", "Not authenticated with JWT token");
        }
        
        return ResponseEntity.ok(tokenInfo);
    }
} 
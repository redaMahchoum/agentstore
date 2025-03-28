package com.agentstore.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User Data Transfer Object")
public class UserDTO {

    @Schema(description = "User ID (UUID)")
    private UUID id;

    @Schema(description = "Keycloak ID")
    private String keycloakId;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username", example = "john.doe", required = true)
    private String username;

    @NotBlank(message = "First name is required")
    @Schema(description = "First name", example = "John", required = true)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name", example = "Doe", required = true)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "john.doe@example.com", required = true)
    private String email;

    @Schema(description = "Password (only used for creation)", example = "SecureP@ss123", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Schema(description = "Account enabled status")
    private boolean enabled;

    @Schema(description = "Email verification status")
    private boolean emailVerified;

    @Schema(description = "User roles")
    private Set<String> roles;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
} 
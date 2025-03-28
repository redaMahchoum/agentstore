package com.agentstore.userservice.controller;

import com.agentstore.userservice.model.dto.UserDTO;
import com.agentstore.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin') or authentication.principal.equals(#username)")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('admin') or authentication.principal.equals(#username)")
    @Operation(summary = "Get user by username", description = "Retrieves a user by their username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    public ResponseEntity<UserDTO> getUserByUsername(
            @Parameter(description = "Username", required = true)
            @PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Create user", description = "Creates a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "User data", required = true)
            @Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(userDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin') or authentication.principal.equals(@userService.getUserById(#id).username)")
    @Operation(summary = "Update user", description = "Updates an existing user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated user data", required = true)
            @Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Delete user", description = "Deletes a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Assign role to user", description = "Assigns a role to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role assigned successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    public ResponseEntity<Void> assignRole(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Role name", required = true)
            @PathVariable String role) {
        userService.assignRole(id, role);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Remove role from user", description = "Removes a role from a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role removed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    public ResponseEntity<Void> removeRole(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Role name", required = true)
            @PathVariable String role) {
        userService.removeRole(id, role);
        return ResponseEntity.noContent().build();
    }
} 
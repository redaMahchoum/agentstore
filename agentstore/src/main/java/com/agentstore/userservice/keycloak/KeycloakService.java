package com.agentstore.userservice.keycloak;

import com.agentstore.userservice.model.dto.UserDTO;

import java.util.List;

public interface KeycloakService {

    String createUser(UserDTO userDTO);
    
    void updateUser(String keycloakId, UserDTO userDTO);
    
    void deleteUser(String keycloakId);
    
    void changePassword(String keycloakId, String currentPassword, String newPassword);
    
    void assignRole(String keycloakId, String role);
    
    void removeRole(String keycloakId, String role);
    
    List<String> getAllRoles();
    
    boolean verifyCredentials(String username, String password);
} 
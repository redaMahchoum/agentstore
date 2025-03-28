package com.agentstore.userservice.keycloak;

import com.agentstore.userservice.exception.KeycloakIntegrationException;
import com.agentstore.userservice.model.dto.UserDTO;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Override
    public String createUser(UserDTO userDTO) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            
            // Validate roles before proceeding
            if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
                List<String> availableRoles = getAllRoles();
                for (String role : userDTO.getRoles()) {
                    if (!availableRoles.contains(role)) {
                        throw new IllegalArgumentException("Invalid role: " + role);
                    }
                }
            }

            // Create a minimal UserRepresentation with only the essential fields
            UserRepresentation user = new UserRepresentation();
            user.setUsername(userDTO.getUsername());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setEnabled(userDTO.isEnabled());
            user.setEmailVerified(userDTO.isEmailVerified());
            // Do not set any other fields here that might cause serialization issues
            
            // Create user with jakarta Response
            Response response = usersResource.create(user);
            
            // Handle error responses
            if (response.getStatus() != 201) {
                String errorMessage = response.readEntity(String.class);
                log.error("Keycloak user creation failed with status {}: {}", response.getStatus(), errorMessage);
                throw new KeycloakIntegrationException("Failed to create user in Keycloak: " + 
                    (errorMessage.isEmpty() ? "Status code " + response.getStatus() : errorMessage));
            }
            
            String userId = CreatedResponseUtil.getCreatedId(response);
            log.info("Created new user in Keycloak with ID: {}", userId);

            // Set password
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                CredentialRepresentation credential = createPasswordCredential(userDTO.getPassword());
                UserResource userResource = usersResource.get(userId);
                userResource.resetPassword(credential);
            }

            // Assign roles after user creation
            if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
                assignRolesToUser(userId, userDTO.getRoles());
            } else {
                // Assign default user role if none specified
                assignRolesToUser(userId, Collections.singleton("user"));
            }

            return userId;
        } catch (Exception e) {
            log.error("Error creating user in Keycloak", e);
            throw new KeycloakIntegrationException("Error creating user in Keycloak: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateUser(String keycloakId, UserDTO userDTO) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(keycloakId);
            UserRepresentation user = userResource.toRepresentation();

            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setEnabled(userDTO.isEnabled());
            user.setEmailVerified(userDTO.isEmailVerified());

            userResource.update(user);
            log.info("Updated user in Keycloak with ID: {}", keycloakId);

            // Update password if provided
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                CredentialRepresentation credential = createPasswordCredential(userDTO.getPassword());
                userResource.resetPassword(credential);
            }

            // Update roles if provided
            if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
                updateUserRoles(keycloakId, userDTO.getRoles());
            }
        } catch (NotFoundException e) {
            log.error("User not found in Keycloak with ID: {}", keycloakId, e);
            throw new KeycloakIntegrationException("User not found in Keycloak with ID: " + keycloakId, e);
        } catch (Exception e) {
            log.error("Error updating user in Keycloak", e);
            throw new KeycloakIntegrationException("Error updating user in Keycloak: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(String keycloakId) {
        try {
            keycloak.realm(realm).users().delete(keycloakId);
            log.info("Deleted user from Keycloak with ID: {}", keycloakId);
        } catch (NotFoundException e) {
            log.error("User not found in Keycloak with ID: {}", keycloakId, e);
            throw new KeycloakIntegrationException("User not found in Keycloak with ID: " + keycloakId, e);
        } catch (Exception e) {
            log.error("Error deleting user from Keycloak", e);
            throw new KeycloakIntegrationException("Error deleting user from Keycloak: " + e.getMessage(), e);
        }
    }

    @Override
    public void changePassword(String keycloakId, String currentPassword, String newPassword) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(keycloakId);
            CredentialRepresentation credential = createPasswordCredential(newPassword);
            userResource.resetPassword(credential);
            log.info("Changed password for user in Keycloak with ID: {}", keycloakId);
        } catch (NotFoundException e) {
            log.error("User not found in Keycloak with ID: {}", keycloakId, e);
            throw new KeycloakIntegrationException("User not found in Keycloak with ID: " + keycloakId, e);
        } catch (Exception e) {
            log.error("Error changing password for user in Keycloak", e);
            throw new KeycloakIntegrationException("Error changing password for user in Keycloak: " + e.getMessage(), e);
        }
    }

    @Override
    public void assignRole(String keycloakId, String role) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(keycloakId);
            RoleRepresentation roleRepresentation = keycloak.realm(realm).roles().get(role).toRepresentation();
            userResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));
            log.info("Assigned role '{}' to user in Keycloak with ID: {}", role, keycloakId);
        } catch (NotFoundException e) {
            log.error("User or role not found in Keycloak", e);
            throw new KeycloakIntegrationException("User or role not found in Keycloak: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error assigning role to user in Keycloak", e);
            throw new KeycloakIntegrationException("Error assigning role to user in Keycloak: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeRole(String keycloakId, String role) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(keycloakId);
            RoleRepresentation roleRepresentation = keycloak.realm(realm).roles().get(role).toRepresentation();
            userResource.roles().realmLevel().remove(Collections.singletonList(roleRepresentation));
            log.info("Removed role '{}' from user in Keycloak with ID: {}", role, keycloakId);
        } catch (NotFoundException e) {
            log.error("User or role not found in Keycloak", e);
            throw new KeycloakIntegrationException("User or role not found in Keycloak: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error removing role from user in Keycloak", e);
            throw new KeycloakIntegrationException("Error removing role from user in Keycloak: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getAllRoles() {
        try {
            return keycloak.realm(realm).roles().list().stream()
                    .map(RoleRepresentation::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving roles from Keycloak", e);
            throw new KeycloakIntegrationException("Error retrieving roles from Keycloak: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyCredentials(String username, String password) {
        // Implementation would typically use the Keycloak client to verify credentials
        // This is a simplified version for demonstration purposes
        try {
            Keycloak tempKeycloak = Keycloak.getInstance(
                    authServerUrl,
                    realm,
                    username,
                    password,
                    "admin-cli");
            
            // If this succeeds, credentials are valid
            tempKeycloak.tokenManager().getAccessToken();
            return true;
        } catch (Exception e) {
            log.warn("Failed to verify credentials for user: {}", username);
            return false;
        }
    }

    private CredentialRepresentation createPasswordCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        return credential;
    }

    private void assignRolesToUser(String userId, Set<String> roles) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        RoleMappingResource roleMappingResource = userResource.roles();

        List<RoleRepresentation> roleRepresentations = roles.stream()
                .map(role -> keycloak.realm(realm).roles().get(role).toRepresentation())
                .collect(Collectors.toList());

        roleMappingResource.realmLevel().add(roleRepresentations);
    }

    private void updateUserRoles(String userId, Set<String> newRoles) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        RoleMappingResource roleMappingResource = userResource.roles();

        // Get current roles
        List<RoleRepresentation> currentRoles = roleMappingResource.realmLevel().listAll();
        
        // Remove all current roles
        if (!currentRoles.isEmpty()) {
            roleMappingResource.realmLevel().remove(currentRoles);
        }
        
        // Assign new roles
        if (!newRoles.isEmpty()) {
            List<RoleRepresentation> roleRepresentations = newRoles.stream()
                    .map(role -> keycloak.realm(realm).roles().get(role).toRepresentation())
                    .collect(Collectors.toList());
            
            roleMappingResource.realmLevel().add(roleRepresentations);
        }
    }
} 
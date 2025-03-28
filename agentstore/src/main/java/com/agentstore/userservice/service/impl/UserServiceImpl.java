package com.agentstore.userservice.service.impl;

import com.agentstore.userservice.exception.ResourceNotFoundException;
import com.agentstore.userservice.keycloak.KeycloakService;
import com.agentstore.userservice.model.dto.UserDTO;
import com.agentstore.userservice.model.entity.User;
import com.agentstore.userservice.model.mapper.UserMapper;
import com.agentstore.userservice.repository.UserRepository;
import com.agentstore.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakService keycloakService;

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Retrieving all users");
        return userMapper.toDTOList(userRepository.findAll());
    }

    @Override
    public UserDTO getUserById(UUID id) {
        log.info("Retrieving user by id: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        log.info("Retrieving user by username: {}", username);
        return userRepository.findByUsername(username)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        log.info("Retrieving user by email: {}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating new user with username: {}", userDTO.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + userDTO.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + userDTO.getEmail());
        }
        
        // Validate password if provided
        if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        // Create user in Keycloak
        String keycloakId = keycloakService.createUser(userDTO);
        userDTO.setKeycloakId(keycloakId);
        
        // Save user in database
        User user = userMapper.toEntity(userDTO);
        User savedUser = userRepository.save(user);
        
        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updateUser(UUID id, UserDTO userDTO) {
        log.info("Updating user with id: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Update user in Keycloak
        keycloakService.updateUser(existingUser.getKeycloakId(), userDTO);
        
        // Update user in database
        userMapper.updateUserEntity(userDTO, existingUser);
        User updatedUser = userRepository.save(existingUser);
        
        return userMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Delete user from Keycloak
        keycloakService.deleteUser(user.getKeycloakId());
        
        // Delete user from database
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void changePassword(UUID id, String currentPassword, String newPassword) {
        log.info("Changing password for user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Change password in Keycloak
        keycloakService.changePassword(user.getKeycloakId(), currentPassword, newPassword);
    }

    @Override
    @Transactional
    public void assignRole(UUID id, String role) {
        log.info("Assigning role '{}' to user with id: {}", role, id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Assign role in Keycloak
        keycloakService.assignRole(user.getKeycloakId(), role);
        
        // Update local user entity
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeRole(UUID id, String role) {
        log.info("Removing role '{}' from user with id: {}", role, id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Remove role in Keycloak
        keycloakService.removeRole(user.getKeycloakId(), role);
        
        // Update local user entity
        user.getRoles().remove(role);
        userRepository.save(user);
    }
} 
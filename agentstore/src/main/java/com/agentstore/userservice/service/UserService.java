package com.agentstore.userservice.service;

import com.agentstore.userservice.model.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserDTO> getAllUsers();
    
    UserDTO getUserById(UUID id);
    
    UserDTO getUserByUsername(String username);
    
    UserDTO getUserByEmail(String email);
    
    UserDTO createUser(UserDTO userDTO);
    
    UserDTO updateUser(UUID id, UserDTO userDTO);
    
    void deleteUser(UUID id);
    
    void changePassword(UUID id, String currentPassword, String newPassword);
    
    void assignRole(UUID id, String role);
    
    void removeRole(UUID id, String role);
} 
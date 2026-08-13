package com.zosh.service;

import com.zosh.enums.UserRole;
import com.zosh.enums.UserStatus;
import com.zosh.payload.dto.UserDTO;
import com.zosh.payload.request.UpdateProfileRequest;

import java.util.List;

public interface AdminUserService {
    
    List<UserDTO> getAllUsers();
    
    UserDTO getUserById(Long id) throws Exception;
    
    UserDTO updateUserProfile(Long id, UpdateProfileRequest request, String adminUsername) throws Exception;
    
    UserDTO updateUserRole(Long id, UserRole role, String adminUsername) throws Exception;
    
    UserDTO updateUserStatus(Long id, UserStatus status, String adminUsername) throws Exception;
    
    void deleteUser(Long id, String adminUsername) throws Exception;
}

package com.zosh.service.Impl;

import com.zosh.enums.UserRole;
import com.zosh.enums.UserStatus;
import com.zosh.mapper.UserMapper;
import com.zosh.model.User;
import com.zosh.payload.dto.UserDTO;
import com.zosh.payload.request.UpdateProfileRequest;
import com.zosh.repository.UserRepository;
import com.zosh.service.AdminUserService;
import com.zosh.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found with id: " + id));
        return UserMapper.toDTO(user);
    }

    @Override
    public UserDTO updateUserProfile(Long id, UpdateProfileRequest request, String adminUsername) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            User existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new Exception("Email is already in use by another account.");
            }
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);

        auditService.logAction(
                adminUsername, 
                "PROFILE_UPDATE", 
                user.getEmail(), 
                "N/A", 
                "Updated Profile"
        );

        return UserMapper.toDTO(updatedUser);
    }

    @Override
    public UserDTO updateUserRole(Long id, UserRole newRole, String adminUsername) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found with id: " + id));

        String oldRole = user.getRole().name();
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        auditService.logAction(
                adminUsername, 
                "ROLE_CHANGE", 
                user.getEmail(), 
                oldRole, 
                newRole.name()
        );

        return UserMapper.toDTO(updatedUser);
    }

    @Override
    public UserDTO updateUserStatus(Long id, UserStatus newStatus, String adminUsername) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found with id: " + id));

        if (user.getRole() == UserRole.ROLE_SYSTEM_ADMIN && newStatus != UserStatus.ACTIVE) {
            long activeAdmins = userRepository.countByRoleAndStatus(UserRole.ROLE_SYSTEM_ADMIN, UserStatus.ACTIVE);
            if (activeAdmins <= 1) {
                throw new Exception("Cannot deactivate the last active System Administrator.");
            }
        }

        String oldStatus = user.getStatus() != null ? user.getStatus().name() : "NULL";
        user.setStatus(newStatus);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        auditService.logAction(
                adminUsername, 
                "STATUS_CHANGE", 
                user.getEmail(), 
                oldStatus, 
                newStatus.name()
        );

        return UserMapper.toDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id, String adminUsername) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found with id: " + id));

        if (user.getRole() == UserRole.ROLE_SYSTEM_ADMIN) {
            long activeAdmins = userRepository.countByRoleAndStatus(UserRole.ROLE_SYSTEM_ADMIN, UserStatus.ACTIVE);
            if (activeAdmins <= 1) {
                throw new Exception("Cannot deactivate the last active System Administrator.");
            }
        }

        String oldStatus = user.getStatus() != null ? user.getStatus().name() : "NULL";
        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditService.logAction(
                adminUsername, 
                "SOFT_DELETE", 
                user.getEmail(), 
                oldStatus, 
                UserStatus.INACTIVE.name()
        );
    }
}

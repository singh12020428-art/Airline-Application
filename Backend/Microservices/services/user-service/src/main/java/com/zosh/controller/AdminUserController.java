package com.zosh.controller;

import com.zosh.payload.dto.UserDTO;
import com.zosh.payload.request.UpdateProfileRequest;
import com.zosh.payload.request.UpdateRoleRequest;
import com.zosh.payload.request.UpdateStatusRequest;
import com.zosh.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUserProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest request,
            @RequestHeader("X-User-Email") String adminEmail
    ) throws Exception {
        UserDTO updatedUser = adminUserService.updateUserProfile(id, request, adminEmail);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request,
            @RequestHeader("X-User-Email") String adminEmail
    ) throws Exception {
        UserDTO updatedUser = adminUserService.updateUserRole(id, request.getRole(), adminEmail);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request,
            @RequestHeader("X-User-Email") String adminEmail
    ) throws Exception {
        UserDTO updatedUser = adminUserService.updateUserStatus(id, request.getStatus(), adminEmail);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String adminEmail
    ) throws Exception {
        adminUserService.deleteUser(id, adminEmail);
        return ResponseEntity.ok("User soft deleted successfully");
    }
}

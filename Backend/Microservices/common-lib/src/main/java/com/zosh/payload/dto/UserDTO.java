package com.zosh.payload.dto;

import com.zosh.enums.UserRole;
import com.zosh.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private UserRole role;

    private UserStatus status;

    private LocalDateTime lastLogin;
}
package com.zosh.payload.request;

import com.zosh.enums.UserRole;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    private UserRole role;
}

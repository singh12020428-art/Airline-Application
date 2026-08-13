package com.zosh.payload.request;

import com.zosh.enums.UserStatus;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    private UserStatus status;
}

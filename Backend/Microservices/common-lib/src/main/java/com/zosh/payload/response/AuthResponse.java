package com.zosh.payload.response;


import com.zosh.payload.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String jwt;
    private String refreshToken;
    private String message;
    private String title;
    private UserDTO user;
}

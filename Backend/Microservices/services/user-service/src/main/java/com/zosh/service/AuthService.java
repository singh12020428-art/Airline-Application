package com.zosh.service;


import com.zosh.payload.dto.UserDTO;
import com.zosh.payload.request.SignupRequest;
import com.zosh.payload.response.AuthResponse;


public interface AuthService {

    AuthResponse login(String email, String password) throws Exception;
    AuthResponse signup(SignupRequest req) throws Exception;
    org.springframework.http.ResponseEntity<AuthResponse> refreshToken(com.zosh.payload.request.RefreshTokenRequest request) throws Exception;
}

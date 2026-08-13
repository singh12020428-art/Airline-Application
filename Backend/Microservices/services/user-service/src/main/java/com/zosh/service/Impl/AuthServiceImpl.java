package com.zosh.service.Impl;

import com.zosh.config.JwtProvider;
import com.zosh.enums.UserRole;
import com.zosh.enums.UserStatus;
import com.zosh.mapper.UserMapper;
import com.zosh.model.User;
import com.zosh.payload.request.SignupRequest;
import com.zosh.payload.response.AuthResponse;
import com.zosh.repository.UserRepository;
import com.zosh.service.AuthService;
import com.zosh.service.RefreshTokenService;
import com.zosh.service.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           CustomUserDetailsService customUserDetailsService,
                           JwtProvider jwtProvider,
                           RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public AuthResponse signup(SignupRequest req) throws Exception {

        User existingUser = userRepository.findByEmail(req.getEmail());

        if (existingUser != null) {
            throw new Exception("Email already registered");
        }

        User newUser = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(savedUser.getEmail());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        String jwt = jwtProvider.generateToken(authentication, savedUser.getId());
        String refreshToken = refreshTokenService.createRefreshToken(savedUser.getId()).getToken();

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setUser(UserMapper.toDTO(savedUser));
        authResponse.setTitle("Welcome " + savedUser.getFullName());
        authResponse.setMessage("Registered Successfully!!");

        return authResponse;
    }

    @Override
    public AuthResponse login(String email, String password) throws Exception {

        Authentication authentication = authenticate(email, password);

        User user = userRepository.findByEmail(email);

        if (user.getStatus() != null && user.getStatus() != UserStatus.ACTIVE) {
            throw new Exception("Account is " + user.getStatus().name() + ". Please contact support.");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String jwt = jwtProvider.generateToken(authentication, user.getId());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setUser(UserMapper.toDTO(user));
        authResponse.setTitle("Welcome " + user.getFullName());
        authResponse.setMessage("Login Successfully!!");

        return authResponse;
    }

    private Authentication authenticate(String email, String password) throws Exception {

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(email);

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new Exception("Invalid Password");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    @Override
    public org.springframework.http.ResponseEntity<AuthResponse> refreshToken(com.zosh.payload.request.RefreshTokenRequest request) throws Exception {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(com.zosh.model.RefreshToken::getUser)
                .map(user -> {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    
                    String token = jwtProvider.generateToken(authentication, user.getId());
                    
                    AuthResponse authResponse = new AuthResponse();
                    authResponse.setJwt(token);
                    authResponse.setRefreshToken(requestRefreshToken);
                    authResponse.setUser(UserMapper.toDTO(user));
                    authResponse.setMessage("Token Refreshed Successfully");
                    return org.springframework.http.ResponseEntity.ok(authResponse);
                })
                .orElseThrow(() -> new Exception("Refresh token is not in database!"));
    }
}
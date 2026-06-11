package com.example.badmintonbooking.service;

import com.example.badmintonbooking.dto.request.LoginRequest;
import com.example.badmintonbooking.dto.request.RefreshTokenRequest;
import com.example.badmintonbooking.dto.response.AuthResponse;
import com.example.badmintonbooking.dto.request.RegisterRequest;
import com.example.badmintonbooking.security.principal.UserPrincipal;

public interface IAuthService {

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse register(RegisterRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(String authorizationHeader, UserPrincipal principal);
}

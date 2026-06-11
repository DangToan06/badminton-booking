package com.example.badmintonbooking.service;

import com.example.badmintonbooking.dto.response.AuthResponse;
import com.example.badmintonbooking.dto.request.RegisterRequest;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
}

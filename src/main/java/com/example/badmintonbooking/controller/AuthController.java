package com.example.badmintonbooking.controller;


import com.example.badmintonbooking.dto.request.LoginRequest;
import com.example.badmintonbooking.dto.request.RefreshTokenRequest;
import com.example.badmintonbooking.dto.response.ApiResponse;
import com.example.badmintonbooking.dto.response.AuthResponse;
import com.example.badmintonbooking.dto.request.RegisterRequest;
import com.example.badmintonbooking.security.principal.UserPrincipal;
import com.example.badmintonbooking.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ){
        AuthResponse authResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registered successfully", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ){
        AuthResponse authResponse = authService.refresh(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Refresh successful", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<AuthResponse>> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            @AuthenticationPrincipal UserPrincipal principal
    ){
        authService.logout(authorizationHeader, principal);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Logged out successfully"));
    }

}

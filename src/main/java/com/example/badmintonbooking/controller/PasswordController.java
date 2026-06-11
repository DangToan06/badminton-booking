package com.example.badmintonbooking.controller;

import com.example.badmintonbooking.dto.request.ChangePasswordRequest;
import com.example.badmintonbooking.dto.request.ForgotPasswordRequest;
import com.example.badmintonbooking.dto.request.ResetPasswordRequest;
import com.example.badmintonbooking.dto.response.ApiResponse;
import com.example.badmintonbooking.security.principal.UserPrincipal;
import com.example.badmintonbooking.service.IPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordController {

    private final IPasswordService passwordService;

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        passwordService.changePassword(request, principal);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("If the email exists, a reset link has been sent.")
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. Please login again."));
    }
}
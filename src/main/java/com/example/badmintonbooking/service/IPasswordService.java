package com.example.badmintonbooking.service;

import com.example.badmintonbooking.dto.request.ChangePasswordRequest;
import com.example.badmintonbooking.dto.request.ForgotPasswordRequest;
import com.example.badmintonbooking.dto.request.ResetPasswordRequest;
import com.example.badmintonbooking.security.principal.UserPrincipal;

public interface IPasswordService {
    void changePassword(ChangePasswordRequest request, UserPrincipal principal);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}

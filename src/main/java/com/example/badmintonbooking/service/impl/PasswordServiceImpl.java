package com.example.badmintonbooking.service.impl;

import com.example.badmintonbooking.dto.request.ChangePasswordRequest;
import com.example.badmintonbooking.dto.request.ForgotPasswordRequest;
import com.example.badmintonbooking.dto.request.ResetPasswordRequest;
import com.example.badmintonbooking.entity.User;
import com.example.badmintonbooking.exception.CustomExceptions;
import com.example.badmintonbooking.repository.UserRepository;
import com.example.badmintonbooking.security.principal.UserPrincipal;
import com.example.badmintonbooking.service.IPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements IPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, ResetTokenInfo> resetTokenStore = new ConcurrentHashMap<>();

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request, UserPrincipal principal) {
        User user = userRepository.findById(principal.user().getId())
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException(principal.user().getId()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomExceptions.WrongPasswordException();
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("User '{}' changed password successfully", user.getUsername());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {

            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

            resetTokenStore.put(token, new ResetTokenInfo(user.getEmail(), expiry));

            log.info("=================================================");
            log.info("PASSWORD RESET EMAIL (SIMULATED)");
            log.info("To: {}", user.getEmail());
            log.info("Reset Token: {}", token);
            log.info("Expires at: {}", expiry);
            log.info("Reset URL: http://localhost:8080/api/v1/auth/reset-password");
            log.info("=================================================");
        });

        log.info("Forgot password requested for email: {}", request.getEmail());
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        ResetTokenInfo tokenInfo = resetTokenStore.get(request.getToken());
        if (tokenInfo == null) {
            throw new RuntimeException("Invalid or expired reset token");
        }

        if (LocalDateTime.now().isAfter(tokenInfo.expiry())) {
            resetTokenStore.remove(request.getToken());
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        User user = userRepository.findByEmail(tokenInfo.email())
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException(tokenInfo.email()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetTokenStore.remove(request.getToken());

        log.info("Password reset successfully for user: '{}'", user.getUsername());
    }


    private record ResetTokenInfo(String email, LocalDateTime expiry) {}
}

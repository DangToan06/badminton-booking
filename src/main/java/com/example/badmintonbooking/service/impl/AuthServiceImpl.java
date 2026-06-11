package com.example.badmintonbooking.service.impl;

import com.example.badmintonbooking.dto.request.LoginRequest;
import com.example.badmintonbooking.dto.request.RefreshTokenRequest;
import com.example.badmintonbooking.dto.response.AuthResponse;
import com.example.badmintonbooking.dto.request.RegisterRequest;
import com.example.badmintonbooking.entity.TokenBlacklist;
import com.example.badmintonbooking.entity.User;
import com.example.badmintonbooking.enums.Role;
import com.example.badmintonbooking.repository.TokenBlacklistRepository;
import com.example.badmintonbooking.repository.UserRepository;
import com.example.badmintonbooking.security.jwt.JwtService;
import com.example.badmintonbooking.security.principal.UserPrincipal;
import com.example.badmintonbooking.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        // AuthenticationManager tự gọi UserDetailsService + BCrypt để xác thực
        // Ném AuthenticationException (→ 401) nếu sai username/password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        UserPrincipal userPrincipal = new UserPrincipal(user);

        String accessToken = jwtService.generateAccessToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);

        log.info("User '{}' logged in successfully", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.CUSTOMER)
                .isEnabled(true)
                .build();

        userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user);

        String accessToken  = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        log.info("New user registered: '{}'", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));

        UserPrincipal userPrincipal = new UserPrincipal(user);

        if(!jwtService.isTokenValid(refreshToken, userPrincipal)){
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(userPrincipal);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void logout(String authorizationHeader, UserPrincipal principal) {
        String token = authorizationHeader.substring(7);

        TokenBlacklist blacklistedToken = TokenBlacklist.builder()
                .token(token)
                .expiryTime(jwtService.extractExpirationAsLocalDateTime(token))
                .user(principal.user())
                .build();

        tokenBlacklistRepository.save(blacklistedToken);
        log.info("Token blacklisted for user: '{}'", principal.getUsername());
    }
}

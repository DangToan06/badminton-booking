package com.example.badmintonbooking.controller;

import com.example.badmintonbooking.dto.request.LoginRequest;
import com.example.badmintonbooking.dto.request.RegisterRequest;
import com.example.badmintonbooking.dto.response.AuthResponse;
import com.example.badmintonbooking.service.IAuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.example.badmintonbooking.exception.GlobalExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = AuthController.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IAuthService authService;

    // Mock các bean Spring Security cần thiết
    @MockitoBean
    private com.example.badmintonbooking.security.jwt.JwtService jwtService;

    @MockitoBean
    private com.example.badmintonbooking.security.principal.UserDetailsServiceImpl userDetailsService;


    // ── Dữ liệu mẫu ───────────────────────────────────────────────────────
    private final AuthResponse mockAuthResponse = AuthResponse.builder()
            .accessToken("mock.access.token")
            .refreshToken("mock.refresh.token")
            .tokenType("Bearer")
            .username("customer1")
            .role("CUSTOMER")
            .build();

    // =====================================================================
    // Test 6: Login thành công → 200 + trả về token
    // =====================================================================

    @Test
    @DisplayName("Test 6: Login đúng credentials → HTTP 200 + AccessToken")
    void login_ValidCredentials_Return200WithToken() throws Exception {
        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setUsername("customer1");
        request.setPassword("123456");

        when(authService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

        // WHEN + THEN
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock.access.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.username").value("customer1"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    // =====================================================================
    // Test 7: Login sai mật khẩu → 401
    // =====================================================================

    @Test
    @DisplayName("Test 7: Login sai password → HTTP 401 Unauthorized")
    void login_WrongPassword_Return401() throws Exception {
        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setUsername("customer1");
        request.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // WHEN + THEN
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    // =====================================================================
    // Test 8: Login thiếu field → 400 Validation Error
    // =====================================================================

    @Test
    @DisplayName("Test 8: Login thiếu username → HTTP 400 Bad Request")
    void login_MissingUsername_Return400() throws Exception {
        // GIVEN - Thiếu username
        LoginRequest request = new LoginRequest();
        request.setPassword("123456");
        // username = null → @NotBlank fail

        // WHEN + THEN
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // Verify authService KHÔNG được gọi khi validation fail
        verify(authService, never()).login(any());
    }

    // =====================================================================
    // Test 9: Register thành công → 201 Created
    // =====================================================================

    @Test
    @DisplayName("Test 9: Register hợp lệ → HTTP 201 Created + Token")
    void register_ValidRequest_Return201() throws Exception {
        // GIVEN
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("123456");
        request.setFullName("Người Dùng Mới");
        request.setEmail("newuser@gmail.com");
        request.setPhoneNumber("0987654321");

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockAuthResponse);

        // WHEN + THEN
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registered successfully"))
                .andExpect(jsonPath("$.data.accessToken").exists());

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    // =====================================================================
    // Test 10: Register email sai format → 400
    // =====================================================================

    @Test
    @DisplayName("Test 10: Register email sai format → HTTP 400 Validation Error")
    void register_InvalidEmail_Return400() throws Exception {
        // GIVEN - Email sai format
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("123456");
        request.setFullName("Người Dùng Mới");
        request.setEmail("not-an-email");  // ← Sai format
        request.setPhoneNumber("0987654321");

        // WHEN + THEN
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(authService, never()).register(any());
    }
}

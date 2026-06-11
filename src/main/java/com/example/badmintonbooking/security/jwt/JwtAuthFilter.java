package com.example.badmintonbooking.security.jwt;

import com.example.badmintonbooking.repository.TokenBlacklistRepository;
import com.example.badmintonbooking.security.principal.UserDetailsServiceImpl;
import com.example.badmintonbooking.security.principal.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
            return;
        } catch (SignatureException | MalformedJwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        // Kiểm tra Blacklist (token đã bị thu hồi khi Logout)
        // Trả về 403 Forbidden dù token còn hạn
        if (tokenBlacklistRepository.existsByToken(jwt)) {
            log.warn("Token is blacklisted for user: {}", username);
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Token has been revoked");
            return;
        }

        // Tải User từ DB và xác thực
        // Chỉ xử lý nếu chưa có Authentication trong SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, principal)) {
                //Tạo Authentication object và đưa vào SecurityContext
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,                        // credentials null vì đã xác thực qua JWT
                                principal.getAuthorities()   // ["ROLE_ADMIN"] / ["ROLE_CUSTOMER"] ...
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Ghi response JSON lỗi trực tiếp (không qua GlobalExceptionHandler
     * vì lỗi xảy ra ở tầng Filter, trước khi vào DispatcherServlet).
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                status,
                status == 401 ? "Unauthorized" : "Forbidden",
                message
        ));
    }
}

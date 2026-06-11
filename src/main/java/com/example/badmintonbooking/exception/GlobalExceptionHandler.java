package com.example.badmintonbooking.exception;

import com.example.badmintonbooking.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = ((FieldError) err).getField();
            fieldErrors.put(field, err.getDefaultMessage());
        });
        String msg = fieldErrors.values().stream().findFirst().orElse("Validation failed");
        log.warn("Validation failed at {}: {}", req.getRequestURI(), fieldErrors);
        return build(HttpStatus.BAD_REQUEST, "Bad Request", msg, req);
    }

    @ExceptionHandler(CustomExceptions.CourtNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleCourtNotAvailable(
            CustomExceptions.CourtNotAvailableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
    }

    @ExceptionHandler(CustomExceptions.InvalidTimeSlotException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimeSlot(
            CustomExceptions.InvalidTimeSlotException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
    }

    @ExceptionHandler(CustomExceptions.InvalidBookingStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBookingStatus(
            CustomExceptions.InvalidBookingStatusException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
    }

    @ExceptionHandler(CustomExceptions.WrongPasswordException.class)
    public ResponseEntity<ErrorResponse> handleWrongPassword(
            CustomExceptions.WrongPasswordException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
    }

    @ExceptionHandler(CustomExceptions.InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(
            CustomExceptions.InvalidFileException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
    }

    // 401 Unauthorized
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid username or password", req);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(
            DisabledException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized",
                "Account is disabled. Please contact administrator.", req);
    }

    // 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Forbidden",
                "You don't have permission to access this resource", req);
    }

    @ExceptionHandler(CustomExceptions.ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            CustomExceptions.ForbiddenException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), req);
    }

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(CustomExceptions.UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            CustomExceptions.UserNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(CustomExceptions.CourtNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCourtNotFound(
            CustomExceptions.CourtNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(CustomExceptions.BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookingNotFound(
            CustomExceptions.BookingNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(CustomExceptions.ClusterNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClusterNotFound(
            CustomExceptions.ClusterNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    // 409 Lịch bị trùng
    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<ErrorResponse> handleBookingConflict(
            BookingConflictException ex, HttpServletRequest req) {
        log.warn("Booking conflict: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req);
    }

    @ExceptionHandler(CustomExceptions.UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameExists(
            CustomExceptions.UsernameAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req);
    }

    @ExceptionHandler(CustomExceptions.EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(
            CustomExceptions.EmailAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req);
    }

    // 503 Lỗi lưu ảnh
    @ExceptionHandler(CloudStorageException.class)
    public ResponseEntity<ErrorResponse> handleCloudStorage(
            CloudStorageException ex, HttpServletRequest req) {
        log.error("Cloud storage error: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                "Cloud storage service is temporarily unavailable. Please try again later.", req);
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}: ", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error,
                                                String message, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}


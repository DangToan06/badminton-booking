package com.example.badmintonbooking.exception;

public class CustomExceptions {

    // 400 Bad Request
    public static class CourtNotAvailableException extends RuntimeException {
        public CourtNotAvailableException(String courtName) {
            super("Court '" + courtName + "' is currently not available for booking");
        }
    }

    public static class InvalidTimeSlotException extends RuntimeException {
        public InvalidTimeSlotException(String timeSlot) {
            super("Time slot '" + timeSlot + "' is not valid");
        }
    }

    public static class InvalidBookingStatusException extends RuntimeException {
        public InvalidBookingStatusException(String action, String currentStatus) {
            super("Cannot " + action + " booking with status: " + currentStatus);
        }
    }

    public static class WrongPasswordException extends RuntimeException {
        public WrongPasswordException() {
            super("Current password is incorrect");
        }
    }

    /** File upload không hợp lệ (sai định dạng, quá dung lượng...) */
    public static class InvalidFileException extends RuntimeException {
        public InvalidFileException(String message) {
            super(message);
        }
    }

    // 403 Forbidden
    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String resource) {
            super("You don't have permission to access this " + resource);
        }
    }

    // 404 Not Found
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(Long id) {
            super("User not found with id: " + id);
        }
        public UserNotFoundException(String username) {
            super("User not found with username: " + username);
        }
    }

    public static class CourtNotFoundException extends RuntimeException {
        public CourtNotFoundException(Long id) {
            super("Court not found with id: " + id);
        }
    }

    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(Long id) {
            super("Booking not found with id: " + id);
        }
    }

    public static class ClusterNotFoundException extends RuntimeException {
        public ClusterNotFoundException(Long id) {
            super("Badminton cluster not found with id: " + id);
        }
    }

    //409 Conflict
    public static class UsernameAlreadyExistsException extends RuntimeException {
        public UsernameAlreadyExistsException(String username) {
            super("Username '" + username + "' already exists");
        }
    }

    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String email) {
            super("Email '" + email + "' already exists");
        }
    }
}
package com.example.badmintonbooking.dto.request;

import com.example.badmintonbooking.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @Size(min = 4, max = 50, message = "Username must be 4-50 characters")
    private String username;

    @Size(min = 2, message = "Full name is too short")
    private String fullName;

    @Email(message = "Email invalid format")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
    private String phoneNumber;

    private Role role;
}

package com.example.badmintonbooking.dto.response;

import com.example.badmintonbooking.entity.User;
import com.example.badmintonbooking.enums.Role;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class UserDTO {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private Boolean isEnabled;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isEnabled(user.getIsEnabled())
                .build();
    }
}

package com.example.badmintonbooking.service.impl;

import com.example.badmintonbooking.dto.request.CreateUserRequest;
import com.example.badmintonbooking.dto.request.UpdateUserRequest;
import com.example.badmintonbooking.dto.response.PageResponse;
import com.example.badmintonbooking.dto.response.UserDTO;
import com.example.badmintonbooking.entity.User;
import com.example.badmintonbooking.enums.Role;
import com.example.badmintonbooking.repository.UserRepository;
import com.example.badmintonbooking.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDTO createUser(CreateUserRequest request) {
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
                .role(request.getRole())
                .isEnabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Admin created new user: '{}' with role: {}", savedUser.getUsername(), savedUser.getRole());

        return UserDTO.fromEntity(savedUser);
    }

    @Override
    public PageResponse<UserDTO> searchUsers(String keyword, Role role, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage = userRepository.searchUsers(keyword, role, pageable);

        List<UserDTO> userDTOs = userPage.getContent()
                .stream()
                .filter(user -> user != null)
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());

        Page<UserDTO> dtoPage = new PageImpl<>(userDTOs, pageable, userPage.getTotalElements());

        return PageResponse.fromPage(dtoPage);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = findUserById(id);
        return UserDTO.fromEntity(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = findUserById(id);

        if (request.getUsername() != null) {
            userRepository.findByUsername(request.getUsername())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new RuntimeException("Username already exists: " + request.getUsername());
                    });
            user.setUsername(request.getUsername());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null) {
            userRepository.findByEmail(request.getEmail())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new RuntimeException("Email already exists: " + request.getEmail());
                    });
            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        User updatedUser = userRepository.save(user);
        log.info("Admin updated user id: {}", id);

        return UserDTO.fromEntity(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
        log.info("Admin deleted user id: {}", id);
    }

    @Override
    public UserDTO lockUser(Long id) {
        findUserById(id);
        userRepository.updateEnabledStatus(id, false);
        log.info("Admin locked user id: {}", id);

        return UserDTO.fromEntity(findUserById(id));
    }

    @Override
    public UserDTO unlockUser(Long id) {
        findUserById(id);
        userRepository.updateEnabledStatus(id, true);
        log.info("Admin unlocked user id: {}", id);

        return UserDTO.fromEntity(findUserById(id));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}

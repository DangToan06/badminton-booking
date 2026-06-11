package com.example.badmintonbooking.controller.admin;

import com.example.badmintonbooking.dto.request.CreateUserRequest;
import com.example.badmintonbooking.dto.request.UpdateUserRequest;
import com.example.badmintonbooking.dto.response.ApiResponse;
import com.example.badmintonbooking.dto.response.PageResponse;
import com.example.badmintonbooking.dto.response.UserDTO;
import com.example.badmintonbooking.enums.Role;
import com.example.badmintonbooking.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final IUserService userService;


    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<UserDTO> result = userService.searchUsers(
                keyword, role, page, size, sortBy, sortDir
        );

        return ResponseEntity.ok(
                ApiResponse.success("Success", result)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Success", user)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserDTO created = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        UserDTO updated = userService.updateUser(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", updated)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<UserDTO>> lockUser(@PathVariable Long id) {
        UserDTO updated = userService.lockUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("User locked successfully", updated)
        );
    }

    @PatchMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<UserDTO>> unlockUser(@PathVariable Long id) {
        UserDTO updated = userService.unlockUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("User unlocked successfully", updated)
        );
    }
}

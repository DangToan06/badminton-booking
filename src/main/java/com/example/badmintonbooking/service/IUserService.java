package com.example.badmintonbooking.service;

import com.example.badmintonbooking.dto.request.CreateUserRequest;
import com.example.badmintonbooking.dto.request.UpdateUserRequest;
import com.example.badmintonbooking.dto.response.PageResponse;
import com.example.badmintonbooking.dto.response.UserDTO;
import com.example.badmintonbooking.enums.Role;

public interface IUserService {

    UserDTO createUser(CreateUserRequest request);

    PageResponse<UserDTO> searchUsers(String keyword, Role role, int page, int size, String sortBy, String sortDir);

    UserDTO getUserById(Long id);

    UserDTO updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    UserDTO lockUser(Long id);

    UserDTO unlockUser(Long id);


}

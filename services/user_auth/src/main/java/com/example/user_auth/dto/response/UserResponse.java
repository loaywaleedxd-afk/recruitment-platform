package com.example.user_auth.dto.response;

import com.example.user_auth.entity.User;
import com.example.user_auth.enums.Role;

import java.util.Set;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        boolean active,
        Set<Role> roles
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.isActive(),
                user.getRoles()
        );
    }
}
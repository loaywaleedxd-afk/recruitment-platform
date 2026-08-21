package com.example.user_auth.dto.request;

import com.example.user_auth.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateRolesRequest(

        @NotEmpty(message = "assign the new role ")
        Set<Role> roles
) {}

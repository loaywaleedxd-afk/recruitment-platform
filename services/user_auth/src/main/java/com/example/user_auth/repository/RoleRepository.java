package com.example.user_auth.repository;

import com.example.user_auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    /*almain usage bta3tha sa3t alregestration a7add
    alrole bta3t alsha5s w bttcheck en alroles dy mwgoda
     */

    Optional<Role> findByName(String name);
}
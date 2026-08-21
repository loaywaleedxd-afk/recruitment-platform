package com.example.user_auth.repository;
import com.example.user_auth.entity.User;
import com.example.user_auth.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRolesContaining(Role role);
}

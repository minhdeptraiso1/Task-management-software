package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository
        extends JpaRepository<User, UUID>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(
            String username
    );

    Optional<User> findByEmailIgnoreCase(
            String email
    );

    boolean existsByUsernameIgnoreCase(
            String username
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    long countByEnabledTrue();

    long countByEnabledFalse();

    List<User> findAllByEnabledTrue();

    long countByRole(
            UserRole role
    );

    long countByRoleAndEnabledTrue(
            UserRole role
    );
}

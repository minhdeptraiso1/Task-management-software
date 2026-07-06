package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseAuditEntity {

    @Column(
            name = "username",
            nullable = false,
            unique = true,
            length = 100
    )
    String username;

    @Column(
            name = "email",
            nullable = false,
            unique = true,
            length = 255
    )
    String email;

    @Column(
            name = "password",
            nullable = false,
            length = 255
    )
    String password;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 50
    )
    UserRole role;

    @Builder.Default
    @Column(
            name = "enabled",
            nullable = false
    )
    boolean enabled = true;

    @Column(name = "logout_all_at")
    Instant logoutAllAt;
}

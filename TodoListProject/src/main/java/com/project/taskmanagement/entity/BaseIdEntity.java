package com.project.taskmanagement.entity;

import com.project.taskmanagement.util.UuidV7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Base entity quản lý UUIDv7 dùng chung.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseIdEntity implements Serializable {

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    protected UUID id = UuidV7Generator.generate();

    /**
     * Phòng trường hợp code nào đó gán id = null
     * trước khi persist.
     */
    @PrePersist
    protected void ensureId() {
        if (id == null) {
            id = UuidV7Generator.generate();
        }
    }
}
package com.project.taskmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseAuditEntity
        extends BaseIdEntity {

    @CreatedDate
    @Column(
            name = "created_at",
            updatable = false
    )
    protected Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    protected Instant updatedAt;

    @CreatedBy
    @Column(
            name = "created_by",
            updatable = false,
            length = 100
    )
    protected String createdBy;

    @LastModifiedBy
    @Column(
            name = "updated_by",
            length = 100
    )
    protected String updatedBy;

    @Column(name = "deleted_at")
    protected Instant deletedAt;

    @Column(
            name = "deleted_by",
            length = 100
    )
    protected String deletedBy;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted(
            String deletedBy
    ) {
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
    }

    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }
}
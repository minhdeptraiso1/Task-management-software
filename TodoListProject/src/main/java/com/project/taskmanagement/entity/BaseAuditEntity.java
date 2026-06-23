package com.project.taskmanagement.entity;

import com.project.taskmanagement.security.CurrentUser;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreRemove;
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
public abstract class BaseAuditEntity {

    @CreatedDate
    Instant createdAt;

    @LastModifiedDate
    Instant updatedAt;

    @CreatedBy
    String createdBy;

    @LastModifiedBy
    String updatedBy;

    Instant deletedAt;

    String deletedBy;

    @PreRemove
    public void preRemove() {
        this.deletedAt = Instant.now();
        this.deletedBy = CurrentUser.username();
    }
}

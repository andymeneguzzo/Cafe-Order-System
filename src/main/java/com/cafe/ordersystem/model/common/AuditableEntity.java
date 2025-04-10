package com.cafe.ordersystem.model.common;

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

import java.time.LocalDateTime;

/**
 * Base class for entities that need to be audited.
 *
 * This abstract class provides common auditing fields:
 * - createdAt: when the entity was created
 * - createdBy: who created the entity
 * - updatedAt: when the entity was last updated
 * - updatedBy: who last updated the entity
 *
 * It uses Spring Data JPA's auditing capabilities via the @EntityListeners annotation.
 */

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditableEntity extends BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(nullable = true, updatable = false, length = 50)
    private String createdBy;

    @LastModifiedDate
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(nullable = true, length = 50)
    private String updatedBy;
}

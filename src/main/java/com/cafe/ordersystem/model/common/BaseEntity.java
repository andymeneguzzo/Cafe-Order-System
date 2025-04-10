package com.cafe.ordersystem.model.common;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for all entities in the system.
 *
 * This abstract class provides common functionality and fields for all entities:
 * - version: used for optimistic locking to prevent concurrent modifications
 *
 * Entities inheriting from this class should define their own ID field
 * because different entities might have different ID types or generation strategies.
 */

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * Version field for optimistic locking.
     *
     * When an entity is updated, Spring Data JPA checks if the version in the database
     * matches the version in memory. If not, it means another process has modified
     * the entity since it was loaded, and an OptimisticLockingFailureException is thrown.
     */
    @Version
    private Long version;
}

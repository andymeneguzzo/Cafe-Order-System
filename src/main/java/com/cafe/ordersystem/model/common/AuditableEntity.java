package com.cafe.ordersystem.model.common;

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
public abstract class AuditableEntity extends BaseEntity {
}

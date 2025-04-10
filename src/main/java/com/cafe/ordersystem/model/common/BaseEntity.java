package com.cafe.ordersystem.model.common;

/**
 * Base class for all entities in the system.
 *
 * This abstract class provides common functionality and fields for all entities:
 * - version: used for optimistic locking to prevent concurrent modifications
 *
 * Entities inheriting from this class should define their own ID field
 * because different entities might have different ID types or generation strategies.
 */
public abstract class BaseEntity {
}

package com.cafe.ordersystem.model.user;

import com.cafe.ordersystem.model.common.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a role in the system for authorization purposes.
 * Each user can have multiple roles, and each role can be assigned to multiple users.
 */

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, unique = true)
    private ERole name;

    /**
     * Enum defining the available roles in the system.
     * - ROLE_ADMIN: System administrators who have full access
     * - ROLE_MANAGER: Cafe managers who can manage products, view reports, etc.
     * - ROLE_STAFF: Cafe staff who can process orders, manage customers, etc.
     */
    public enum ERole {
        ROLE_ADMIN, ROLE_MANAGER, ROLE_STAFF
    }

    @Column(length = 255)
    private String description; // not necessary, added just in case

    /**
     * Constructor for creating a role with just a name
     *
     * @param name The name of the role
     */
    public Role(ERole name) {
        this.name = name;
    }

    /**
     * Returns a string representation of the role
     *
     * @return The name of the role
     */
    @Override
    public String toString() {
        return this.name.name();
    }

    /**
     * Equality is based on the role name, not the ID
     * This ensures that roles with the same name are considered equal
     * even if they have different IDs (which should not happen in practice)
     */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;
        return name == role.name;
    }

    /**
     * Hash code is based on the role name for consistency with equals
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

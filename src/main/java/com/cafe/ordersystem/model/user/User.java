package com.cafe.ordersystem.model.user;

import com.cafe.ordersystem.model.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user account in the system.
 * This includes staff, managers, and admins who have access to the system.
 * Customers are represented by a separate entity.
 */

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(max = 100)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;

    // A user can have multiple roles (e.g., an admin might also be a manager)
    // CascadeType.PERSIST means when a User is persisted, its roles should be persisted too
    // FetchType.EAGER means roles are always fetched when a user is fetched
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Returns the full name of the user.
     *
     * @return The user's full name (first name + last name)
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Adds a role to the user.
     *
     * @param role The role to add
     */
    public void addRole(Role role) {
        roles.add(role);
    }

    /**
     * Removes a role from the user.
     *
     * @param role The role to remove
     */
    public void removeRole(Role role) {
        roles.remove(role);
    }

    /**
     * Checks if the user has a specific role.
     *
     * @param roleName The name of the role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().name().equals(roleName));
    }
}

package com.cafe.ordersystem.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class implements UserDetails from Spring Security and serves as an adapter
 * between our User entity and Spring Security's authentication system.
 *
 * It wraps a User entity and provides security-related information about the user
 * such as authorities (roles), account expiration status, etc.
 */

@Data
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private boolean active;

    // Authorities to represent user roles/permissions in Spring Sec.
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Factory method to create a UserPrincipal from a User entity
     *
     * @param user The User entity
     * @return A UserPrincipal representing the user
     */
    public static UserPrincipal create(User user) {

        // Convert User roles to Spring Sec authorities
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        // build user with all info and roles converted to Spring Sec auth.ties
        return UserPrincipal.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .active(user.isActive())
                .authorities(authorities)
                .build();
    }

    /**
     * Returns the user's full name (first name + last name)
     *
     * @return The user's full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Indicates whether the user's account has expired.
     * In our implementation, accounts never expire.
     *
     * @return true if the user's account is valid (not expired)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     * In our implementation, accounts are never locked.
     *
     * @return true if the user is not locked
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) has expired.
     * In our implementation, credentials never expire.
     *
     * @return true if the user's credentials are valid (not expired)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * This is based on the 'active' flag in our User entity.
     *
     * @return true if the user is enabled
     */
    public boolean isEnabled() {
        return active;
    }

    /**
     * Checks if the user has a specific role
     *
     * @param roleName The name of the role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleName));
    }
}

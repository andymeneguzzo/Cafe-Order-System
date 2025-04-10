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



}

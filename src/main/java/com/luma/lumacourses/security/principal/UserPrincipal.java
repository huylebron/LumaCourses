package com.luma.lumacourses.security.principal;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.luma.lumacourses.entity.User;
import com.luma.lumacourses.util.enums.Role;

import lombok.Getter;

/**
 * Wraps {@link User} entity as Spring Security's UserDetails.
 * Exposes userId and role for use in service/controller layer.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String username;
    private final String password;
    private final Role role;
    private final boolean active;

    private UserPrincipal(User user) {
        this.userId   = user.getId();
        this.email    = user.getEmail();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.role     = user.getRole();
        this.active   = user.isActive();
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /** Spring Security username = email (used as authentication principal) */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}

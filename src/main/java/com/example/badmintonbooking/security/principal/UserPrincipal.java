package com.example.badmintonbooking.security.principal;

import com.example.badmintonbooking.entity.User;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


public record UserPrincipal(User user) implements UserDetails {


    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @NullMarked
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Tài khoản không bao giờ hết hạn trong hệ thống này
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Tài khoản bị khóa khi isEnabled = false (Admin khóa)
     */
    @Override
    public boolean isAccountNonLocked() {
        return user.getIsEnabled();
    }

    /**
     * Credentials không bao giờ hết hạn (JWT lo phần này)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Tài khoản active khi isEnabled = true
     */
    @Override
    public boolean isEnabled() {
        return user.getIsEnabled();
    }
}
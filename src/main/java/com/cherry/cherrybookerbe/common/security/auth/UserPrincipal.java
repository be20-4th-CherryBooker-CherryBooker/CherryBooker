package com.cherry.cherrybookerbe.common.security.auth;

import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UserPrincipal(Integer userId, String email, String name, String role) implements UserDetails {

    @Builder
    public UserPrincipal {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        //사용자 식별하는 고유 값 반환
        return String.valueOf(this.userId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getUserId(),
                user.getUserEmail(),
                user.getUserName(),
                user.getUserRole().name()
        );
    }
}

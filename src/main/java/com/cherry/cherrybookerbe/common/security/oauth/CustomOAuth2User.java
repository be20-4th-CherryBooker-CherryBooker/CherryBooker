package com.cherry.cherrybookerbe.common.security.oauth;

import com.cherry.cherrybookerbe.common.security.auth.UserPrincipal;
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public record CustomOAuth2User(User user, Map<String, Object> attributes, boolean isNewUser) implements OAuth2User {

    public UserPrincipal toUserPrincipal() {
        return UserPrincipal.builder()
                .userId(user.getUserId())
                .email(user.getUserEmail())
                .name(user.getUserName())
                .role(user.getUserRole().name())
                .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getUserRole()));
    }

    @Override
    public String getName() {
        return user.getUserName();
    }
}

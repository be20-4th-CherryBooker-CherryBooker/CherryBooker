package com.cherry.cherrybookerbe.common.security.oauth;

import com.cherry.cherrybookerbe.auth.domain.oauth.GoogleUserInfo;
import com.cherry.cherrybookerbe.auth.domain.oauth.KakaoUserInfo;
import com.cherry.cherrybookerbe.auth.domain.oauth.OAuth2UserInfo;
import com.cherry.cherrybookerbe.user.command.domain.entity.LoginType;
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import com.cherry.cherrybookerbe.user.command.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String providerId = userRequest.getClientRegistration().getRegistrationId();
        LoginType loginType = LoginType.valueOf(providerId.toUpperCase());

        OAuth2UserInfo userInfo = switch (loginType) {
            case GOOGLE -> new GoogleUserInfo(oAuth2User.getAttributes());
            case KAKAO -> new KakaoUserInfo(oAuth2User.getAttributes());
        };

        User user = userRepository.findByUserEmail(userInfo.getEmail()).orElse(null);
        boolean isNewUser = false;

        if (user == null) {

            // "이름 → 닉네임"
            String nickname = userInfo.getName();
            if (nickname == null || nickname.isBlank()) {
                // name도 없으면 이메일 앞부분, 그것도 없으면 랜덤
                String email = userInfo.getEmail();
                if (email != null && !email.isBlank()) {
                    nickname = email.split("@")[0];
                } else {
                    nickname = "user_" + UUID.randomUUID().toString().substring(0, 8);
                }
            }

            user = User.builder()
                    .userEmail(userInfo.getEmail())
                    .userName(userInfo.getName())
                    .userNickname(nickname)
                    .loginType(loginType)
                    .build();

            userRepository.save(user);
            isNewUser = true;
        }
        return new CustomOAuth2User(user, oAuth2User.getAttributes(), isNewUser);
    }
}


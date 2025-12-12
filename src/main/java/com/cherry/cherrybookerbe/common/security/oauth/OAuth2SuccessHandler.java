package com.cherry.cherrybookerbe.common.security.oauth;

import com.cherry.cherrybookerbe.common.security.auth.RefreshTokenStore;
import com.cherry.cherrybookerbe.common.security.auth.UserPrincipal;
import com.cherry.cherrybookerbe.common.security.jwt.JwtTokenProvider;
import com.cherry.cherrybookerbe.user.command.domain.entity.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore tokenStore;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {

        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

            // 정지된 계정 토큰 발급 차단
            if (oAuth2User.user().getUserStatus() == UserStatus.SUSPENDED) {
                log.warn("정지된 계정(email={})의 로그인이 차단되었습니다.", oAuth2User.user().getUserEmail());
                String redirectUrl = "http://localhost:5173/oauth2/error/suspended";
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                return;
            }

            UserPrincipal principal = oAuth2User.toUserPrincipal();

            String userId = principal.userId().toString();

            String email = principal.email() != null ? principal.email() : "";
            String name = principal.name() != null ? principal.name() : "Unknown";
            String role = principal.role() != null ? principal.role().toString() : "ROLE_USER"; // Role이 Enum일 경우 toString()

            // Access Token 생성
            String accessToken = jwtTokenProvider.createAccessToken(
                    userId,
                    Map.of(
                            "email", email,
                            "name", name,
                            "role", role  // 반드시 문자열
                    )
            );

            // Refresh Token 생성
            String refreshToken = jwtTokenProvider.createRefreshToken(userId);



            // Redis 저장
            tokenStore.save(
                    userId,
                    refreshToken,
                    jwtTokenProvider.getRefreshExpSeconds()
            );

            // RefreshToken → Cookie 저장
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(14 * 24 * 60 * 60)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            // 프론트로 redirect
            String redirectUrl = "http://localhost:5173/oauth2/success"
                    + "?accessToken=" + accessToken;

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 Success 처리 중 오류", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 처리 실패");
            } catch (Exception ignored) {}
        }
    }
}

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

        final String reqUri = request.getRequestURI();
        final String query = request.getQueryString();
        log.info("[OAUTH2-SUCCESS] entry uri={} query={} authType={} isAuthenticated={}",
                reqUri, query, authentication != null ? authentication.getClass().getSimpleName() : "null",
                authentication != null && authentication.isAuthenticated());

        try {
            Object principalObj = authentication != null ? authentication.getPrincipal() : null;
            log.info("[OAUTH2-SUCCESS] principalClass={}",
                    principalObj != null ? principalObj.getClass().getName() : "null");

            if (!(principalObj instanceof CustomOAuth2User oAuth2User)) {
                log.error("[OAUTH2-SUCCESS] Unexpected principal type: {}", principalObj);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected principal");
                return;
            }

            // (1) OAuth2User 내부 user / status 확인
            log.info("[OAUTH2-SUCCESS] userEmail={} userStatus={}",
                    safe(oAuth2User.user().getUserEmail()),
                    oAuth2User.user().getUserStatus());

            if (oAuth2User.user().getUserStatus() == UserStatus.SUSPENDED) {
                log.warn("[OAUTH2-SUCCESS] suspended user blocked email={}", safe(oAuth2User.user().getUserEmail()));
                String redirectUrl = "http://localhost:5173/oauth2/error/suspended";
                log.info("[OAUTH2-SUCCESS] redirect -> {}", redirectUrl);
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                return;
            }

            // (2) UserPrincipal 변환 결과 확인
            UserPrincipal principal = oAuth2User.toUserPrincipal();
            log.info("[OAUTH2-SUCCESS] principal userId={} email={} name={} role={}",
                    principal != null ? principal.userId() : null,
                    principal != null ? safe(principal.email()) : null,
                    principal != null ? safe(principal.name()) : null,
                    principal != null ? principal.role() : null
            );

            if (principal == null || principal.userId() == null) {
                log.error("[OAUTH2-SUCCESS] principal or userId is null. cannot issue tokens.");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Principal is null");
                return;
            }

            String userId = principal.userId().toString();
            String email = principal.email() != null ? principal.email() : "";
            String name  = principal.name()  != null ? principal.name()  : "Unknown";
            String role  = principal.role()  != null ? principal.role().toString() : "ROLE_USER";

            // (3) 토큰 발급 로그
            log.info("[OAUTH2-SUCCESS] issuing tokens userId={} role={}", userId, role);

            String accessToken = jwtTokenProvider.createAccessToken(
                    userId,
                    Map.of("email", email, "name", name, "role", role)
            );
            log.info("[OAUTH2-SUCCESS] accessToken issued length={}", accessToken != null ? accessToken.length() : -1);

            String refreshToken = jwtTokenProvider.createRefreshToken(userId);
            log.info("[OAUTH2-SUCCESS] refreshToken issued length={}", refreshToken != null ? refreshToken.length() : -1);

            // (4) Redis 저장 전/후 로그
            long ttl = jwtTokenProvider.getRefreshExpSeconds();
            log.info("[OAUTH2-SUCCESS] saving refresh token to redis userId={} ttlSeconds={}", userId, ttl);
            tokenStore.save(userId, refreshToken, ttl);
            log.info("[OAUTH2-SUCCESS] redis save OK userId={}", userId);

            // (5) 쿠키 세팅 로그
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(14 * 24 * 60 * 60)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            log.info("[OAUTH2-SUCCESS] set-cookie added (httpOnly={}, secure={}, sameSite={}, path={})",
                    true, true, "None", "/");

            // (6) 최종 redirect
            String redirectUrl = "http://localhost:5173/oauth2/success" + "?accessToken=" + accessToken;
            log.info("[OAUTH2-SUCCESS] redirect -> {}", abbreviate(redirectUrl, 200));
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

            log.info("[OAUTH2-SUCCESS] done userId={}", userId);

        } catch (Exception e) {
            log.error("[OAUTH2-SUCCESS] exception occurred. uri={} query={}", reqUri, query, e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 처리 실패");
            } catch (Exception ignored) {}
        }
    }

    private String safe(String v) { return v == null ? "null" : v; }
    private String abbreviate(String s, int max) {
        if (s == null) return "null";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}


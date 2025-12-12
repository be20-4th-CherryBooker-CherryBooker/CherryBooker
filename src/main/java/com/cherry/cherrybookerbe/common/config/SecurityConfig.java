package com.cherry.cherrybookerbe.common.config;

import com.cherry.cherrybookerbe.common.security.jwt.JwtAuthenticationFilter;
import com.cherry.cherrybookerbe.common.security.jwt.JwtTokenProvider;
import com.cherry.cherrybookerbe.common.security.oauth.OAuth2SuccessHandler;
import com.cherry.cherrybookerbe.common.security.oauth.OAuth2UserService;
import com.cherry.cherrybookerbe.user.command.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2UserService oAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ========================================================
                // 기본 설정
                // ========================================================
                .csrf(csrf -> csrf.disable())   //CORS 허용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  //JWT 구조이므로 세션 비활성화
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"message\":\"Unauthorized\"}");
                }))

                // ========================================================
                // 인가 설정
                // ========================================================
                .authorizeHttpRequests(auth -> auth
                        // Kubernetes health check를 위한 설정
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/", "/error", "/favicon.ico",
                                "/login/**",
                                "/oauth2/**",
                                "/oauth2/authorization/**",
                                "/login/oauth2/**",
                                "/login/oauth2/code/**",
                                "/auth/**",
                                "/auth/login/kakao/**"
                        ).permitAll()
                        // 2) 관리자 전용 API
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )


                        // ========================================================
                // OAuth2 설정
                // ========================================================
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(u -> u.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                );

        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                UsernamePasswordAuthenticationFilter.class
        );


        return http.build();
    }

    // ======================================================================
    // Password Encoder
    // ======================================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ======================================================================
    // CORS 설정
    // ======================================================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}


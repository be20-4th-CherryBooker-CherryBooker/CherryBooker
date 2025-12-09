package com.cherry.cherrybookerbe.auth.dto.response.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private boolean isNewUser;
}

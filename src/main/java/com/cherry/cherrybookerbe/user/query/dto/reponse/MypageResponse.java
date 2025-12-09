package com.cherry.cherrybookerbe.user.query.dto.reponse;

import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MypageResponse {

    private String email;
    private String name;
    private String nickname;

    @Builder
    private MypageResponse(String email, String name,  String nickname) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
    }

    public static MypageResponse from(User user) {
        return MypageResponse.builder()
                .email(user.getUserEmail())
                .name(user.getUserName())
                .nickname(user.getUserNickname())
                .build();
    }

}

package com.cherry.cherrybookerbe.user.query.dto.reponse;

import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import com.cherry.cherrybookerbe.user.command.domain.entity.UserRole;
import com.cherry.cherrybookerbe.user.command.domain.entity.UserStatus;
import lombok.Builder;

import java.time.LocalDateTime;

public class UserListResponse {

    private Integer userId;
    private String email;
    private String name;
    private String nickname;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createAt;

    @Builder
    private UserListResponse(Integer userId, String email, String name, String nickname, UserRole role, UserStatus status, LocalDateTime createdAt){
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.role =  role;
        this.status = status;
        this.createAt = createdAt;
    }

    public static UserListResponse from(User user){
        return UserListResponse.builder()
                .userId(user.getUserId())
                .email(user.getUserEmail())
                .name(user.getUserName())
                .nickname(user.getUserNickname())
                .role(user.getUserRole())
                .status(user.getUserStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

}

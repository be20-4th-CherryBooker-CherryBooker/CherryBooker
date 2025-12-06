package com.cherry.cherrybookerbe.user.command.domain.entity;

import com.cherry.cherrybookerbe.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_user_email", columnNames = "user_email"),
                @UniqueConstraint(name = "uk_user_nickname", columnNames = "user_nickname")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 20)
    private LoginType loginType;

    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;

    @Column(name = "user_email", nullable = false, length = 50)
    private String userEmail;

    @Column(name = "user_password", length = 300)
    private String userPassword;   // 소셜 로그인일 땐 null 가능

    @Column(name = "user_nickname", nullable = false, length = 20)
    private String userNickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 20)
    private UserStatus userStatus = UserStatus.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    private UserRole userRole = UserRole.USER;
}

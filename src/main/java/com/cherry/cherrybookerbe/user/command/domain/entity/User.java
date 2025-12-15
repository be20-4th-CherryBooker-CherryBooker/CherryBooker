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
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", length = 20)
    private LoginType loginType;

    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;

    @Column(name = "user_email", nullable = false, length = 50)
    private String userEmail;

    @Column(name = "user_password", length = 300)
    private String userPassword;

    @Column(name = "user_nickname", nullable = false, length = 20)
    private String userNickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 20)
    private UserStatus userStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    private UserRole userRole;

    @Column(name = "delete_count", nullable = false)
    private Integer deleteCount;

    @Builder
    private User(LoginType loginType,
                 String userName,
                 String userEmail,
                 String userPassword,
                 String userNickname,
                 UserRole userRole) {
        this.loginType = loginType;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userNickname = userNickname;
        this.deleteCount = 0;      //기본값 0

        // 생성 시 기본 상태
        this.userStatus = UserStatus.NORMAL;
        this.userRole = (userRole != null) ? userRole : UserRole.USER;
    }

    // 닉네임 변경 메소드
    public void updateNickName(String newNickName){
        this.userNickname = newNickName;
    }

    //회원 탈퇴
    // User.java

    public void softDelete() {
        // 이미 탈퇴 처리된 경우 중복 처리 방지
        if (this.userStatus == UserStatus.DELETE) {
            return;
        }
        this.userStatus = UserStatus.DELETE;
    }

}
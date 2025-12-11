package com.cherry.cherrybookerbe.notification.command.domain.entity;

import com.cherry.cherrybookerbe.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "alarm_log") // DB 테이블은 그대로 사용
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Integer id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Integer userId;

    @Column(name = "alarm_title", nullable = false, length = 150)
    private String title;

    @Column(name = "alarm_context", nullable = false, length = 255)
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Builder
    private Notification(Integer userId, String title, String content) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.read = false;
    }

    public void markRead() {
        this.read = true;
    }
}

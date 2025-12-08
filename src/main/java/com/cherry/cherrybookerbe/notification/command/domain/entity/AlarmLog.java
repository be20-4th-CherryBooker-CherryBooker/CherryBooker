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
@Table(name = "alarm_log")
public class AlarmLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Integer id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Integer userId;

    @Column(name = "alarm_title", nullable = false, length = 150)
    private String alarmTitle;

    @Column(name = "alarm_context", nullable = false, length = 255)
    private String alarmContext;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Builder
    private AlarmLog(Integer userId,
                     String alarmTitle,
                     String alarmContext) {
        this.userId = userId;
        this.alarmTitle = alarmTitle;
        this.alarmContext = alarmContext;
    }

    public void markRead() {
        this.read = true;
    }
}

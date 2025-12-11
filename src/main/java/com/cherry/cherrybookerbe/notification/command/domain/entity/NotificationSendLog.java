package com.cherry.cherrybookerbe.notification.command.domain.entity;

import com.cherry.cherrybookerbe.common.model.entity.BaseTimeEntity;
import com.cherry.cherrybookerbe.notification.command.domain.enums.NotificationSendStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "alarm_send_log")
public class NotificationSendLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "send_log_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private NotificationTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_status", nullable = false, length = 20)
    private NotificationSendStatus status;

    @Column(name = "template_context", nullable = false, length = 255)
    private String bodySnapshot;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Builder
    private NotificationSendLog(NotificationTemplate template,
                                NotificationSendStatus status,
                                String bodySnapshot,
                                LocalDateTime sentAt) {
        this.template = template;
        this.status = status != null ? status : NotificationSendStatus.PENDING;
        this.bodySnapshot = bodySnapshot;
        this.sentAt = sentAt != null ? sentAt : LocalDateTime.now();
    }

    public void markSuccess() {
        this.status = NotificationSendStatus.SUCCESS;
    }

    public void markFail() {
        this.status = NotificationSendStatus.FAILED;
    }
}

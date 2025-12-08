package com.cherry.cherrybookerbe.notification.command.domain.entity;

import com.cherry.cherrybookerbe.notification.command.domain.enums.AlarmSendStatus;
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
public class AlarmSendLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "send_log_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private AlarmTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_status", nullable = false, length = 20)
    private AlarmSendStatus logStatus;

    @Column(name = "template_context", nullable = false, length = 255)
    private String templateContext;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private AlarmSendLog(AlarmTemplate template,
                         AlarmSendStatus logStatus,
                         String templateContext,
                         LocalDateTime sentAt) {
        this.template = template;
        this.logStatus = logStatus != null ? logStatus : AlarmSendStatus.PENDING;
        this.templateContext = templateContext;
        this.sentAt = sentAt != null ? sentAt : LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markSuccess() {
        this.logStatus = AlarmSendStatus.SUCCESS;
    }

    public void markFail() {
        this.logStatus = AlarmSendStatus.FAILED;
    }
}

package com.cherry.cherrybookerbe.notification.query.dto.response;

import com.cherry.cherrybookerbe.notification.command.domain.entity.NotificationSendLog;
import com.cherry.cherrybookerbe.notification.command.domain.enums.NotificationSendStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationSendLogResponse {

    private final Integer sendLogId;
    private final Integer templateId;
    private final String templateTitle;
    private final NotificationSendStatus status;
    private final String bodySnapshot;
    private final LocalDateTime sentAt;

    public static NotificationSendLogResponse from(NotificationSendLog log) {
        return NotificationSendLogResponse.builder()
                .sendLogId(log.getId())
                .templateId(log.getTemplate().getId())
                .templateTitle(log.getTemplate().getTitle())
                .status(log.getStatus())
                .bodySnapshot(log.getBodySnapshot())
                .sentAt(log.getSentAt())
                .build();
    }
}

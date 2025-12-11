package com.cherry.cherrybookerbe.notification.query.dto.response;

import com.cherry.cherrybookerbe.notification.command.domain.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationSummaryResponse {

    private final Integer notificationId;
    private final String title;
    private final String content;
    private final boolean read;
    private final LocalDateTime createdAt;

    public static NotificationSummaryResponse from(Notification notification) {
        return NotificationSummaryResponse.builder()
                .notificationId(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

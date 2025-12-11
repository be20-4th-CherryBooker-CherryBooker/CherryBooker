package com.cherry.cherrybookerbe.notification.command.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationCreatedEvent {

    private final Integer userId;
    private final Integer notificationId;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
    private final long unreadCount;

    public static NotificationCreatedEvent of(Integer userId,
                                              Integer notificationId,
                                              String title,
                                              String content,
                                              LocalDateTime createdAt,
                                              long unreadCount) {
        return NotificationCreatedEvent.builder()
                .userId(userId)
                .notificationId(notificationId)
                .title(title)
                .content(content)
                .createdAt(createdAt)
                .unreadCount(unreadCount)
                .build();
    }
}

package com.cherry.cherrybookerbe.notification.command.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationReadEvent {

    private final Integer userId;
    private final long unreadCount;

    public static NotificationReadEvent of(Integer userId, long unreadCount) {
        return NotificationReadEvent.builder()
                .userId(userId)
                .unreadCount(unreadCount)
                .build();
    }
}

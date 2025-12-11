package com.cherry.cherrybookerbe.notification.query.sse;

import com.cherry.cherrybookerbe.notification.command.event.NotificationCreatedEvent;
import com.cherry.cherrybookerbe.notification.command.event.NotificationReadEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationSseEmitters emitters;

    // 새로운 알림이 생성되었을 때
    @EventListener
    public void onNotificationCreated(NotificationCreatedEvent event) {
        emitters.sendToUser(event.getUserId().longValue(), "NOTIFICATION", event);
    }

    // 읽음 처리되었을 때 (미읽음 카운트 재전달)
    @EventListener
    public void onNotificationRead(NotificationReadEvent event) {
        emitters.sendToUser(event.getUserId().longValue(), "UNREAD_COUNT", event.getUnreadCount());
    }
}

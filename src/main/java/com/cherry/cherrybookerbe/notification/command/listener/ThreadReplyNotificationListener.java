package com.cherry.cherrybookerbe.notification.command.listener;

import com.cherry.cherrybookerbe.notification.command.event.ThreadReplyCreatedEvent;
import com.cherry.cherrybookerbe.notification.command.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ThreadReplyNotificationListener {

    private final NotificationCommandService notificationCommandService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReplyCreated(ThreadReplyCreatedEvent event) {
        notificationCommandService.notifyThreadReply(
                event.threadOwnerUserId(),
                event.rootThreadId(),
                event.replyWriterNickname()
        );
    }
}

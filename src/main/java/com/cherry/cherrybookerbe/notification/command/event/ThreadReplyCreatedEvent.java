package com.cherry.cherrybookerbe.notification.command.event;

public record ThreadReplyCreatedEvent(
        Integer rootThreadId,
        Integer threadOwnerUserId,
        Integer replyWriterUserId,
        String replyWriterNickname
) {}

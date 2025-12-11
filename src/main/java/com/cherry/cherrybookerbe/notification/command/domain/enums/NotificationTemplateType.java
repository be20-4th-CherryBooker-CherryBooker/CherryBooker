package com.cherry.cherrybookerbe.notification.command.domain.enums;

public enum NotificationTemplateType {
    // 시스템 공지
    SYSTEM,

    // 이벤트성 알림들
    EVENT_THREAD_REPLY,      // 글귀 스레드에 답글 달림
    EVENT_THREAD_REPORTED   // 스레드가 신고됨
}

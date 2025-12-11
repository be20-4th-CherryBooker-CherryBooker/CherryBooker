package com.cherry.cherrybookerbe.notification.command.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDispatchResponse {
    private final Integer notificationId;
}

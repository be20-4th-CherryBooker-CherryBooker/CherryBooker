package com.cherry.cherrybookerbe.notification.command.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class NotificationBroadcastRequest {
    private Map<String, String> variables;
}

package com.cherry.cherrybookerbe.notification.command.dto.response;

import com.cherry.cherrybookerbe.notification.command.domain.entity.NotificationTemplate;
import com.cherry.cherrybookerbe.notification.command.domain.enums.NotificationTemplateType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationTemplateResponse {

    private final Integer templateId;
    private final NotificationTemplateType templateType;
    private final String title;
    private final String body;

    public static NotificationTemplateResponse from(NotificationTemplate template) {
        return NotificationTemplateResponse.builder()
                .templateId(template.getId())
                .templateType(template.getType())
                .title(template.getTitle())
                .body(template.getBody())
                .build();
    }
}

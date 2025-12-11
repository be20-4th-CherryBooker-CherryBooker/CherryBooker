package com.cherry.cherrybookerbe.notification.query.dto.response;

import com.cherry.cherrybookerbe.common.dto.Pagination;
import com.cherry.cherrybookerbe.notification.command.dto.response.NotificationTemplateResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationTemplatePageResponse {

    private final java.util.List<NotificationTemplateResponse> templates;
    private final Pagination pagination;
}

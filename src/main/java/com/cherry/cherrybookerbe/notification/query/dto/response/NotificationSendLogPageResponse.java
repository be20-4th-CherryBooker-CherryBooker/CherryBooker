package com.cherry.cherrybookerbe.notification.query.dto.response;

import com.cherry.cherrybookerbe.common.dto.Pagination;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationSendLogPageResponse {

    private final java.util.List<NotificationSendLogResponse> logs;
    private final Pagination pagination;
}


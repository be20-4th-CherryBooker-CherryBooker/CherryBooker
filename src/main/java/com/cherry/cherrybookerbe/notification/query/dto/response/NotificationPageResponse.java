package com.cherry.cherrybookerbe.notification.query.dto.response;

import com.cherry.cherrybookerbe.common.dto.Pagination;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationPageResponse {

    private final List<NotificationSummaryResponse> notifications;
    private final Pagination pagination;
}

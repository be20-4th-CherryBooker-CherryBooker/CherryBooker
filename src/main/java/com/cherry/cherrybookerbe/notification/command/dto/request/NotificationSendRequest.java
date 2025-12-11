package com.cherry.cherrybookerbe.notification.command.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class NotificationSendRequest {

    @NotNull(message = "발송 대상 사용자 ID는 필수 값입니다.")
    private Integer targetUserId;

    // 템플릿 내부 {{key}} 치환용
    private Map<String, String> variables;

    @Builder
    private NotificationSendRequest(Integer targetUserId,
                                    Map<String, String> variables) {
        this.targetUserId = targetUserId;
        this.variables = variables;
    }
}

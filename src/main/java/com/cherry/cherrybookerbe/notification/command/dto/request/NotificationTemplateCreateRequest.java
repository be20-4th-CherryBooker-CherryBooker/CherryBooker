package com.cherry.cherrybookerbe.notification.command.dto.request;

import com.cherry.cherrybookerbe.notification.command.domain.enums.NotificationTemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationTemplateCreateRequest {

    @NotNull(message = "템플릿 종류는 필수 값입니다.")
    private NotificationTemplateType templateType;

    @NotBlank(message = "템플릿 제목은 필수 값입니다.")
    @Size(max = 50, message = "템플릿 제목은 50자를 넘을 수 없습니다.")
    private String title;

    @NotBlank(message = "템플릿 본문은 필수 값입니다.")
    private String body;

    @Builder
    private NotificationTemplateCreateRequest(NotificationTemplateType templateType,
                                              String title,
                                              String body) {
        this.templateType = templateType;
        this.title = title;
        this.body = body;
    }
}

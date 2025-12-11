package com.cherry.cherrybookerbe.notification.command.init;

import com.cherry.cherrybookerbe.notification.command.domain.entity.NotificationTemplate;
import com.cherry.cherrybookerbe.notification.command.domain.enums.NotificationTemplateType;
import com.cherry.cherrybookerbe.notification.command.domain.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationTemplateInitializer implements ApplicationRunner {

    private final NotificationTemplateRepository templateRepository;

    @Override
    public void run(ApplicationArguments args) {
        initEventTemplate(
                NotificationTemplateType.EVENT_THREAD_REPLY,
                "답변 알림 - 내 글귀에 답변 도착",
                "사용자 {{writerNickname}} 님이 스레드 {{threadId}} 에 답변을 남겼습니다."
        );

        initEventTemplate(
                NotificationTemplateType.EVENT_THREAD_REPORTED,
                "신고 알림 - 스레드가 신고되었습니다",
                "당신의 스레드 {{threadId}} 가 신고되었습니다."
        );
    }

    private void initEventTemplate(NotificationTemplateType type, String title, String body) {

        templateRepository.findByTypeAndDeletedFalse(type)
                .ifPresentOrElse(
                        // 이미 있으면 삭제만 풀어주고 내용은 그대로 두거나
                        template -> {
                            // 혹시 삭제 상태면 복구
                            if (template.isDeleted()) {
                                template.restore(); // deleted = false로 바꾸는 메서드 이미 있음
                            }
                        },
                        // 없으면 새로 생성
                        () -> {
                            NotificationTemplate template = NotificationTemplate.builder()
                                    .type(type)
                                    .title(title)
                                    .body(body)
                                    .build();
                            templateRepository.save(template);
                        }
                );
    }
}

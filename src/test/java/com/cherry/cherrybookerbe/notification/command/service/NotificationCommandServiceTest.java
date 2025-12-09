package com.cherry.cherrybookerbe.notification.command.service;

import com.cherry.cherrybookerbe.notification.command.domain.entity.Notification;
import com.cherry.cherrybookerbe.notification.command.domain.entity.NotificationSendLog;
import com.cherry.cherrybookerbe.notification.command.domain.entity.NotificationTemplate;
import com.cherry.cherrybookerbe.notification.command.domain.enums.NotificationSendStatus;
import com.cherry.cherrybookerbe.notification.command.domain.enums.NotificationTemplateType;
import com.cherry.cherrybookerbe.notification.command.domain.repository.NotificationRepository;
import com.cherry.cherrybookerbe.notification.command.domain.repository.NotificationSendLogRepository;
import com.cherry.cherrybookerbe.notification.command.domain.repository.NotificationTemplateRepository;
import com.cherry.cherrybookerbe.notification.command.dto.request.NotificationSendRequest;
import com.cherry.cherrybookerbe.notification.command.dto.request.NotificationTemplateCreateRequest;
import com.cherry.cherrybookerbe.notification.command.dto.request.NotificationTemplateUpdateRequest;
import com.cherry.cherrybookerbe.notification.command.dto.response.NotificationDispatchResponse;
import com.cherry.cherrybookerbe.notification.command.dto.response.NotificationTemplateResponse;
import com.cherry.cherrybookerbe.notification.command.event.NotificationCreatedEvent;
import com.cherry.cherrybookerbe.notification.command.event.NotificationReadEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationSendLogRepository sendLogRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NotificationCommandService notificationCommandService;

    // ============ 템플릿 CUD ============

    @Test
    @DisplayName("NTF-001: 관리자는 템플릿을 등록할 수 있다")
    void createTemplate_createsNotificationTemplate() {
        // given
        NotificationTemplateCreateRequest request = NotificationTemplateCreateRequest.builder()
                .templateType(NotificationTemplateType.EVENT)
                .title("답변 알림 템플릿")
                .body("{{nickname}}님이 답변을 남겼습니다.")
                .build();

        NotificationTemplate saved = NotificationTemplate.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .type(request.getTemplateType())
                .build();
        ReflectionTestUtils.setField(saved, "id", 1);

        when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(saved);

        // when
        NotificationTemplateResponse response = notificationCommandService.createTemplate(request);

        // then
        assertThat(response.getTemplateId()).isEqualTo(1);
        assertThat(response.getTemplateType()).isEqualTo(NotificationTemplateType.EVENT);
        assertThat(response.getTitle()).isEqualTo("답변 알림 템플릿");
    }

    @Test
    @DisplayName("NTF-003: 관리자는 템플릿의 제목/본문/타입을 수정할 수 있다")
    void updateTemplate_updatesTemplateFields() {
        // given
        NotificationTemplate existing = NotificationTemplate.builder()
                .title("기존 제목")
                .body("기존 본문")
                .type(NotificationTemplateType.SYSTEM)
                .build();
        ReflectionTestUtils.setField(existing, "id", 1);

        when(templateRepository.findById(1)).thenReturn(Optional.of(existing));

        NotificationTemplateUpdateRequest request = NotificationTemplateUpdateRequest.builder()
                .templateType(NotificationTemplateType.EVENT)
                .title("수정된 제목")
                .body("수정된 본문")
                .build();

        // when
        NotificationTemplateResponse response = notificationCommandService.updateTemplate(1, request);

        // then
        assertThat(existing.getTitle()).isEqualTo("수정된 제목");
        assertThat(existing.getBody()).isEqualTo("수정된 본문");
        assertThat(existing.getType()).isEqualTo(NotificationTemplateType.EVENT);

        assertThat(response.getTemplateId()).isEqualTo(1);
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
    }

    @Test
    @DisplayName("NTF-004: 관리자는 템플릿을 삭제(소프트 삭제)할 수 있다")
    void deleteTemplate_marksTemplateDeleted() {
        // given
        NotificationTemplate template = NotificationTemplate.builder()
                .title("삭제할 템플릿")
                .body("본문")
                .type(NotificationTemplateType.SYSTEM)
                .build();
        ReflectionTestUtils.setField(template, "id", 1);

        when(templateRepository.findById(1)).thenReturn(Optional.of(template));

        // when
        notificationCommandService.deleteTemplate(1);

        // then
        assertThat(template.isDeleted()).isTrue();
    }

    // ============ 템플릿 기반 발송 ============

    @Test
    @DisplayName("NTF-005: 템플릿 기반으로 알림을 발송하고 로그/이벤트를 기록한다")
    void sendByTemplate_sendsNotificationAndPublishesEvent() {
        // given
        NotificationTemplate template = NotificationTemplate.builder()
                .title("신고 {{count}}건 발생")
                .body("{{nickname}}님, 새로운 신고가 있습니다.")
                .type(NotificationTemplateType.SYSTEM)
                .build();
        ReflectionTestUtils.setField(template, "id", 10);

        when(templateRepository.findById(10)).thenReturn(Optional.of(template));

        NotificationSendRequest request = NotificationSendRequest.builder()
                .targetUserId(1)
                .variables(Map.of(
                        "count", "3",
                        "nickname", "체리"
                ))
                .build();

        // Notification 저장 결과 스텁
        Notification savedNotification = Notification.builder()
                .userId(1)
                .title("신고 3건 발생")
                .content("체리님, 새로운 신고가 있습니다.")
                .build();
        ReflectionTestUtils.setField(savedNotification, "id", 100);
        ReflectionTestUtils.setField(savedNotification, "createdAt", LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // 로그 저장 스텁
        when(sendLogRepository.save(any(NotificationSendLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(notificationRepository.countByUserIdAndReadFalse(1)).thenReturn(5L);

        // 트랜잭션 동기화 활성화
        TransactionSynchronizationManager.initSynchronization();
        try {
            // when
            NotificationDispatchResponse response =
                    notificationCommandService.sendByTemplate(10, request);

            // then: 저장된 알림 ID 반환
            assertThat(response.getNotificationId()).isEqualTo(100);

            // 템플릿 merge 검증
            ArgumentCaptor<Notification> notificationCaptor =
                    ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(notificationCaptor.capture());

            Notification toSave = notificationCaptor.getValue();
            assertThat(toSave.getUserId()).isEqualTo(1);
            assertThat(toSave.getTitle()).isEqualTo("신고 3건 발생");
            assertThat(toSave.getContent()).contains("체리님");

            // afterCommit 수동 호출 → 이벤트 발행 검증
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();
            }

            ArgumentCaptor<NotificationCreatedEvent> eventCaptor =
                    ArgumentCaptor.forClass(NotificationCreatedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            NotificationCreatedEvent event = eventCaptor.getValue();
            assertThat(event.getUserId()).isEqualTo(1);
            assertThat(event.getNotificationId()).isEqualTo(100);
            assertThat(event.getUnreadCount()).isEqualTo(5L);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("삭제된 템플릿으로 알림 발송 시 예외가 발생한다")
    void sendByTemplate_deletedTemplate_throwsException() {
        // given
        NotificationTemplate template = NotificationTemplate.builder()
                .title("삭제된 템플릿")
                .body("본문")
                .type(NotificationTemplateType.SYSTEM)
                .build();
        template.markDeleted();
        when(templateRepository.findById(1)).thenReturn(Optional.of(template));

        NotificationSendRequest request = NotificationSendRequest.builder()
                .targetUserId(1)
                .build();

        // when & then
        assertThatThrownBy(() -> notificationCommandService.sendByTemplate(1, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 템플릿");
    }

    // ============ 알림함 읽음 / 삭제 ============

    @Test
    @DisplayName("알림함 - 단일 알림 읽음 처리 후 미읽음 개수를 이벤트로 발행한다")
    void markRead_marksNotificationReadAndPublishesEvent() {
        // given
        Notification notification = Notification.builder()
                .userId(1)
                .title("테스트 알림")
                .content("내용")
                .build();
        ReflectionTestUtils.setField(notification, "id", 100);
        ReflectionTestUtils.setField(notification, "read", false);

        when(notificationRepository.findById(100)).thenReturn(Optional.of(notification));
        when(notificationRepository.countByUserIdAndReadFalse(1)).thenReturn(3L);

        TransactionSynchronizationManager.initSynchronization();
        try {
            // when
            notificationCommandService.markRead(1, 100);

            // then: 읽음 처리
            assertThat(notification.isRead()).isTrue();

            // afterCommit 수동 호출 → 이벤트 발행 검증
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();
            }

            ArgumentCaptor<NotificationReadEvent> eventCaptor =
                    ArgumentCaptor.forClass(NotificationReadEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            NotificationReadEvent event = eventCaptor.getValue();
            assertThat(event.getUserId()).isEqualTo(1);
            assertThat(event.getUnreadCount()).isEqualTo(3L);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("다른 사용자의 알림을 읽음 처리하려 하면 예외가 발생한다")
    void markRead_otherUsersNotification_throwsException() {
        // given
        Notification notification = Notification.builder()
                .userId(1)
                .title("알림")
                .content("내용")
                .build();
        ReflectionTestUtils.setField(notification, "id", 100);

        when(notificationRepository.findById(100)).thenReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationCommandService.markRead(2, 100))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("다른 사용자의 알림");
    }

    @Test
    @DisplayName("알림함 - 모든 미읽음 알림을 읽음 처리하고 미읽음 개수를 이벤트로 발행한다")
    void markAllRead_marksAllUnreadAsReadAndPublishesEvent() {
        // given
        Notification n1 = Notification.builder()
                .userId(1)
                .title("알림1")
                .content("내용1")
                .build();
        ReflectionTestUtils.setField(n1, "id", 1);
        ReflectionTestUtils.setField(n1, "read", false);

        Notification n2 = Notification.builder()
                .userId(1)
                .title("알림2")
                .content("내용2")
                .build();
        ReflectionTestUtils.setField(n2, "id", 2);
        ReflectionTestUtils.setField(n2, "read", false);

        when(notificationRepository.findByUserIdAndReadFalse(1))
                .thenReturn(List.of(n1, n2));
        when(notificationRepository.countByUserIdAndReadFalse(1)).thenReturn(0L);

        TransactionSynchronizationManager.initSynchronization();
        try {
            // when
            notificationCommandService.markAllRead(1);

            // then: 모두 읽음 처리
            assertThat(n1.isRead()).isTrue();
            assertThat(n2.isRead()).isTrue();

            // afterCommit 수동 호출
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();
            }

            ArgumentCaptor<NotificationReadEvent> eventCaptor =
                    ArgumentCaptor.forClass(NotificationReadEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            NotificationReadEvent event = eventCaptor.getValue();
            assertThat(event.getUserId()).isEqualTo(1);
            assertThat(event.getUnreadCount()).isEqualTo(0L);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("알림함 - 미읽음 알림이 없으면 아무 작업도 하지 않는다")
    void markAllRead_noUnread_doNothing() {
        // given
        when(notificationRepository.findByUserIdAndReadFalse(1))
                .thenReturn(List.of());

        // when
        notificationCommandService.markAllRead(1);

        // then
        verify(notificationRepository, never()).countByUserIdAndReadFalse(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("알림함 - 단일 알림 삭제는 자신의 알림만 가능하다")
    void deleteNotification_deletesOwnNotification() {
        // given
        Notification notification = Notification.builder()
                .userId(1)
                .title("알림")
                .content("내용")
                .build();
        ReflectionTestUtils.setField(notification, "id", 100);

        when(notificationRepository.findById(100)).thenReturn(Optional.of(notification));

        // when
        notificationCommandService.deleteNotification(1, 100);

        // then
        verify(notificationRepository).delete(notification);
    }

    @Test
    @DisplayName("알림함 - 다른 사용자의 알림 삭제 시 예외 발생")
    void deleteNotification_otherUsersNotification_throwsException() {
        // given
        Notification notification = Notification.builder()
                .userId(1)
                .title("알림")
                .content("내용")
                .build();
        ReflectionTestUtils.setField(notification, "id", 100);

        when(notificationRepository.findById(100)).thenReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationCommandService.deleteNotification(2, 100))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("다른 사용자의 알림");
    }

    @Test
    @DisplayName("알림함 - 사용자는 읽은 알림들을 일괄 삭제할 수 있다")
    void deleteAllRead_deletesAllReadNotifications() {
        // when
        notificationCommandService.deleteAllRead(1);

        // then
        verify(notificationRepository).deleteByUserIdAndReadTrue(1);
    }
}

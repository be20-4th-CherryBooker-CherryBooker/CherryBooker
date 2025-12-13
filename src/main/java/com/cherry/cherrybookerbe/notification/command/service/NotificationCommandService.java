package com.cherry.cherrybookerbe.notification.command.service;

import com.cherry.cherrybookerbe.notification.command.domain.entity.Notification;
import com.cherry.cherrybookerbe.notification.command.domain.entity.NotificationSendLog;
import com.cherry.cherrybookerbe.notification.command.domain.entity.NotificationTemplate;
import com.cherry.cherrybookerbe.notification.command.domain.enums.NotificationSendStatus;
import com.cherry.cherrybookerbe.notification.command.domain.enums.NotificationTemplateType;
import com.cherry.cherrybookerbe.notification.command.domain.repository.NotificationRepository;
import com.cherry.cherrybookerbe.notification.command.domain.repository.NotificationSendLogRepository;
import com.cherry.cherrybookerbe.notification.command.domain.repository.NotificationTemplateRepository;
import com.cherry.cherrybookerbe.notification.command.dto.request.NotificationBroadcastRequest;
import com.cherry.cherrybookerbe.notification.command.dto.request.NotificationSendRequest;
import com.cherry.cherrybookerbe.notification.command.dto.request.NotificationTemplateCreateRequest;
import com.cherry.cherrybookerbe.notification.command.dto.request.NotificationTemplateUpdateRequest;
import com.cherry.cherrybookerbe.notification.command.dto.response.NotificationDispatchResponse;
import com.cherry.cherrybookerbe.notification.command.dto.response.NotificationTemplateResponse;
import com.cherry.cherrybookerbe.notification.command.event.NotificationCreatedEvent;
import com.cherry.cherrybookerbe.notification.command.event.NotificationReadEvent;
import com.cherry.cherrybookerbe.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "transactionManager")
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationSendLogRepository sendLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    // ============ 템플릿 CUD ============

    public NotificationTemplateResponse createTemplate(NotificationTemplateCreateRequest request) {
        NotificationTemplate template = NotificationTemplate.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .type(request.getTemplateType())
                .build();

        NotificationTemplate saved = templateRepository.save(template);
        return NotificationTemplateResponse.from(saved);
    }

    public NotificationTemplateResponse updateTemplate(Integer templateId,
                                                       NotificationTemplateUpdateRequest request) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."));


        template.update(request.getTitle(), request.getBody(), request.getTemplateType());
        return NotificationTemplateResponse.from(template);
    }

    public void deleteTemplate(Integer templateId) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."));

        // EVENT_* 타입은 삭제 금지
        if (template.getType() != NotificationTemplateType.SYSTEM) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "이 템플릿은 삭제할 수 없습니다. (시스템 이벤트 템플릿)");
        }

        template.markDeleted();
    }

    // ============ 템플릿 기반 발송 ============

    public NotificationDispatchResponse sendByTemplate(Integer templateId,
                                                       NotificationSendRequest request) {

        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."));

        if (template.isDeleted()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "삭제된 템플릿입니다.");
        }

        String mergedTitle = mergeVariables(template.getTitle(), request.getVariables());
        String mergedBody = mergeVariables(template.getBody(), request.getVariables());

        Notification notification = Notification.builder()
                .userId(request.getTargetUserId())
                .title(mergedTitle)
                .content(mergedBody)
                .build();

        Notification saved = notificationRepository.save(notification);

        notificationRepository.flush();

        if (saved.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "알림 저장은 되었으나 PK(alarm_id)를 생성/조회하지 못했습니다. alarm_log PK/auto_increment 및 타입을 점검하세요."
            );
        }

        long unreadAfterInsert = notificationRepository.countByUserIdAndReadFalse(request.getTargetUserId());

        // 발송 로그 기록
        NotificationSendLog logEntity = NotificationSendLog.builder()
                .template(template)
                .status(NotificationSendStatus.SUCCESS)
                .bodySnapshot(mergedBody)
                .sentAt(LocalDateTime.now())
                .build();
        sendLogRepository.save(logEntity);

        Integer nid = saved.getId();
        LocalDateTime createdAt = saved.getCreatedAt();

        // 커밋 후 SSE 이벤트 (새 알림 + 미읽음 카운트)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(NotificationCreatedEvent.of(
                        request.getTargetUserId(),
                        nid,
                        mergedTitle,
                        mergedBody,
                        createdAt,
                        unreadAfterInsert
                ));
            }
        });

        return NotificationDispatchResponse.builder()
                .notificationId(saved.getId())
                .build();
    }

    public void sendToAllByTemplate(Integer templateId, NotificationBroadcastRequest request) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."));

        if (template.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제된 템플릿입니다.");
        }
        if (template.getType() != NotificationTemplateType.SYSTEM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SYSTEM 템플릿만 전체 발송할 수 있습니다.");
        }

        Map<String, String> vars = (request == null) ? null : request.getVariables();

        String mergedTitle = mergeVariables(template.getTitle(), vars);
        String mergedBody  = mergeVariables(template.getBody(), vars);

        List<Integer> userIds = userRepository.findAllUserIds();

        List<Notification> notifications = userIds.stream()
                .map(uid -> Notification.builder()
                        .userId(uid)
                        .title(mergedTitle)
                        .content(mergedBody)
                        .build())
                .toList();

        // ID/createdAt 확보하려면 반환값을 받는 게 좋습니다.
        List<Notification> savedList = notificationRepository.saveAll(notifications);

        NotificationSendLog logEntity = NotificationSendLog.builder()
                .template(template)
                .status(NotificationSendStatus.SUCCESS)
                .bodySnapshot(mergedBody)
                .sentAt(LocalDateTime.now())
                .build();
        sendLogRepository.save(logEntity);

        // 커밋 후 SSE 이벤트
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (Notification saved : savedList) {
                    Integer uid = saved.getUserId();

                    long unread = notificationRepository.countByUserIdAndReadFalse(uid);

                    eventPublisher.publishEvent(NotificationCreatedEvent.of(
                            uid,
                            saved.getId(),
                            saved.getTitle(),
                            saved.getContent(),
                            saved.getCreatedAt(),
                            unread
                    ));
                }
            }
        });
    }


    // ============ 읽음 / 삭제 ============

    public void markRead(Integer userId, Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));

        if (!Objects.equals(notification.getUserId(), userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "다른 사용자의 알림은 변경할 수 없습니다.");
        }

        if (!notification.isRead()) {
            notification.markRead();
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                long unread = notificationRepository.countByUserIdAndReadFalse(userId);
                eventPublisher.publishEvent(NotificationReadEvent.of(userId, unread));
            }
        });
    }

    public void markAllRead(Integer userId) {
        List<Notification> unreadList =
                notificationRepository.findByUserIdAndReadFalse(userId);

        if (unreadList.isEmpty()) {
            return;
        }

        unreadList.forEach(Notification::markRead);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                long unread = notificationRepository.countByUserIdAndReadFalse(userId);
                eventPublisher.publishEvent(NotificationReadEvent.of(userId, unread));
            }
        });
    }

    public void deleteNotification(Integer userId, Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));

        if (!Objects.equals(notification.getUserId(), userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "다른 사용자의 알림은 삭제할 수 없습니다.");
        }

        notificationRepository.delete(notification);
    }

    public void deleteAllRead(Integer userId) {
        notificationRepository.deleteByUserIdAndReadTrue(userId);
    }

    // ============ 헬퍼 ============

    private String mergeVariables(String text, Map<String, String> variables) {
        if (text == null || variables == null || variables.isEmpty()) {
            return text;
        }

        String merged = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            merged = merged.replace(placeholder, Objects.toString(entry.getValue(), ""));
        }
        return merged;
    }

    @Transactional(transactionManager = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void notifyThreadReply(Integer targetUserId, Integer threadId, String writerNickname) {

        NotificationTemplate template = templateRepository
                .findByTypeAndDeletedFalse(NotificationTemplateType.EVENT_THREAD_REPLY)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "EVENT_THREAD_REPLY 템플릿이 없습니다."
                ));

        NotificationSendRequest req = NotificationSendRequest.builder()
                .targetUserId(targetUserId)
                .variables(Map.of(
                        "writerNickname", writerNickname,
                        "threadId", String.valueOf(threadId)
                ))
                .build();

        sendByTemplate(template.getId(), req);
    }
}

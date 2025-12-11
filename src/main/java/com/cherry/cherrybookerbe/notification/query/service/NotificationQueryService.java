package com.cherry.cherrybookerbe.notification.query.service;

import com.cherry.cherrybookerbe.common.dto.Pagination;
import com.cherry.cherrybookerbe.notification.command.domain.entity.NotificationSendLog;
import com.cherry.cherrybookerbe.notification.command.domain.entity.NotificationTemplate;
import com.cherry.cherrybookerbe.notification.command.domain.repository.NotificationRepository;
import com.cherry.cherrybookerbe.notification.command.domain.repository.NotificationSendLogRepository;
import com.cherry.cherrybookerbe.notification.command.domain.repository.NotificationTemplateRepository;
import com.cherry.cherrybookerbe.notification.command.dto.response.NotificationTemplateResponse;
import com.cherry.cherrybookerbe.notification.query.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationSendLogRepository sendLogRepository;

    public NotificationPageResponse getMyNotifications(Integer userId, int page, Integer size) {
        int pageSize = resolveSize(size);
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<com.cherry.cherrybookerbe.notification.command.domain.entity.Notification> result =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationSummaryResponse> items = result.getContent().stream()
                .map(NotificationSummaryResponse::from)
                .toList();

        Pagination pagination = Pagination.builder()
                .currentPage(result.getNumber())
                .totalPages(result.getTotalPages())
                .totalItems(result.getTotalElements())
                .build();

        return NotificationPageResponse.builder()
                .notifications(items)
                .pagination(pagination)
                .build();
    }

    public long getMyUnreadCount(Integer userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public NotificationTemplatePageResponse getTemplates(String keyword, int page, Integer size) {
        int pageSize = resolveSize(size);
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<NotificationTemplate> result;
        if (keyword == null || keyword.isBlank()) {
            result = templateRepository.findByDeletedFalse(pageable);
        } else {
            result = templateRepository.findByDeletedFalseAndTitleContainingIgnoreCase(keyword, pageable);
        }

        List<NotificationTemplateResponse> items = result.getContent().stream()
                .map(NotificationTemplateResponse::from)
                .toList();

        Pagination pagination = Pagination.builder()
                .currentPage(result.getNumber())
                .totalPages(result.getTotalPages())
                .totalItems(result.getTotalElements())
                .build();

        return NotificationTemplatePageResponse.builder()
                .templates(items)
                .pagination(pagination)
                .build();
    }

    public NotificationSendLogPageResponse getSendLogs(int page, Integer size) {
        int pageSize = resolveSize(size);
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                pageSize,
                Sort.by(Sort.Direction.DESC, "sentAt")
        );

        Page<NotificationSendLog> result =
                sendLogRepository.findAllByOrderBySentAtDesc(pageable);

        List<NotificationSendLogResponse> items = result.getContent().stream()
                .map(NotificationSendLogResponse::from)
                .toList();

        Pagination pagination = Pagination.builder()
                .currentPage(result.getNumber())
                .totalPages(result.getTotalPages())
                .totalItems(result.getTotalElements())
                .build();

        return NotificationSendLogPageResponse.builder()
                .logs(items)
                .pagination(pagination)
                .build();
    }

    private int resolveSize(Integer size) {
        return (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
    }
}

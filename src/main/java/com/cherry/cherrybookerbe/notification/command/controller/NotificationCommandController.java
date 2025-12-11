package com.cherry.cherrybookerbe.notification.command.controller;

import com.cherry.cherrybookerbe.common.dto.ApiResponse; // 프로젝트 공통 응답 DTO가 있다면 사용, 없으면 직접 정의해서 사용
import com.cherry.cherrybookerbe.notification.command.dto.request.NotificationSendRequest;
import com.cherry.cherrybookerbe.notification.command.dto.request.NotificationTemplateCreateRequest;
import com.cherry.cherrybookerbe.notification.command.dto.request.NotificationTemplateUpdateRequest;
import com.cherry.cherrybookerbe.notification.command.dto.response.NotificationDispatchResponse;
import com.cherry.cherrybookerbe.notification.command.dto.response.NotificationTemplateResponse;
import com.cherry.cherrybookerbe.notification.command.service.NotificationCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class NotificationCommandController {

    private final NotificationCommandService commandService;

    // ===== 사용자 알림 읽음/삭제 (/me) =====

    @PatchMapping("/api/notifications/me/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Integer notificationId,
            @AuthenticationPrincipal(expression = "userId") Integer userId // principal.id 에 맞춰 수정
    ) {
        commandService.markRead(userId, notificationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/api/notifications/me/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) {
        if (userId == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }
        commandService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/api/notifications/me/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Integer notificationId,
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) {
        commandService.deleteNotification(userId, notificationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/api/notifications/me/read")
    public ResponseEntity<ApiResponse<Void>> deleteAllRead(
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) {
        if (userId == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }
        commandService.deleteAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ===== 관리자 템플릿 CUD =====

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/notifications/templates")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> createTemplate(
            @Valid @RequestBody NotificationTemplateCreateRequest request
    ) {
        var res = commandService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/notifications/templates/{templateId}")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> updateTemplate(
            @PathVariable Integer templateId,
            @Valid @RequestBody NotificationTemplateUpdateRequest request
    ) {
        var res = commandService.updateTemplate(templateId, request);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/notifications/templates/{templateId}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @PathVariable Integer templateId
    ) {
        commandService.deleteTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ===== 관리자 템플릿 기반 발송 =====

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/notifications/templates/{templateId}/send")
    public ResponseEntity<ApiResponse<NotificationDispatchResponse>> sendByTemplate(
            @PathVariable Integer templateId,
            @Valid @RequestBody NotificationSendRequest request
    ) {
        var res = commandService.sendByTemplate(templateId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(res));
    }
}

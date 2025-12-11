package com.cherry.cherrybookerbe.notification.query.controller;

import com.cherry.cherrybookerbe.common.dto.ApiResponse;
import com.cherry.cherrybookerbe.notification.query.dto.response.NotificationPageResponse;
import com.cherry.cherrybookerbe.notification.query.dto.response.NotificationSendLogPageResponse;
import com.cherry.cherrybookerbe.notification.query.dto.response.NotificationTemplatePageResponse;
import com.cherry.cherrybookerbe.notification.query.service.NotificationQueryService;
import com.cherry.cherrybookerbe.notification.query.sse.NotificationSseEmitters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class NotificationQueryController {

    private final NotificationQueryService queryService;
    private final NotificationSseEmitters emitters;

    // ===== 내 알림 목록 =====

    @GetMapping("/api/notifications/me")
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) {
        if (userId == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        var res = queryService.getMyNotifications(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/api/notifications/me/unread-count")
    public ResponseEntity<ApiResponse<Long>> getMyUnreadCount(
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) {
        if (userId == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        long count = queryService.getMyUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // ===== SSE 구독 (/me) =====

    @Operation(
            summary = "알림 SSE 스트림 구독",
            description = """
                    현재 로그인한 사용자의 알림 이벤트를 SSE(Server-Sent Events)로 실시간 구독한다.
                    이벤트 타입: INIT, NOTIFICATION, UNREAD_COUNT, PING
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "SSE 스트림 연결 성공",
                    content = @Content(mediaType = "text/event-stream")
            )
    })
    @GetMapping(
            value = "/api/notifications/me/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter stream(
            @AuthenticationPrincipal(expression = "userId") Integer userId
    ) {
        if (userId == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }
        return emitters.add(userId.longValue()); // emitters가 Long 기반이라면 Long으로 캐스팅
    }

    // ===== 관리자 템플릿 목록/검색 =====

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/notifications/templates")
    public ResponseEntity<ApiResponse<NotificationTemplatePageResponse>> getTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        var res = queryService.getTemplates(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    // ===== 관리자 발송 로그 목록 =====

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/notifications/send-logs")
    public ResponseEntity<ApiResponse<NotificationSendLogPageResponse>> getSendLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        var res = queryService.getSendLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}

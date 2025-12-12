package com.cherry.cherrybookerbe.community.command.controller;

import com.cherry.cherrybookerbe.common.dto.ApiResponse;
import com.cherry.cherrybookerbe.common.security.auth.UserPrincipal;
import com.cherry.cherrybookerbe.community.command.dto.request.CreateCommunityReplyRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.CreateCommunityThreadRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.UpdateCommunityReplyRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.UpdateCommunityThreadRequest;
import com.cherry.cherrybookerbe.community.command.dto.response.CommunityReplyCommandResponse;
import com.cherry.cherrybookerbe.community.command.dto.response.CommunityThreadCommandResponse;
import com.cherry.cherrybookerbe.community.command.service.CommunityThreadCommandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community/threads")
public class CommunityThreadCommandController {

    private final CommunityThreadCommandService communityThreadCommandService;

    public CommunityThreadCommandController(CommunityThreadCommandService communityThreadCommandService) {
        this.communityThreadCommandService = communityThreadCommandService;
    }

    // 커뮤니티의 최초 스레드를 생성하는 메소드
    @PostMapping
    public ResponseEntity<ApiResponse<CommunityThreadCommandResponse>> createThread(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCommunityThreadRequest request
    ) {
        if (principal == null || principal.userId() == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        Integer userId = principal.userId();
        CommunityThreadCommandResponse response =
                communityThreadCommandService.createThread(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 기존 커뮤니티 스레드를 수정하는 메소드
    @PutMapping("/{threadId}")
    public ResponseEntity<ApiResponse<CommunityThreadCommandResponse>> updateThread(
            @PathVariable Integer threadId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateCommunityThreadRequest request
    ) {
        if (principal == null || principal.userId() == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        Integer userId = principal.userId();
        CommunityThreadCommandResponse response =
                communityThreadCommandService.updateThread(threadId, userId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 기존 커뮤니티 스레드를 삭제하는 메소드
    @DeleteMapping("/{threadId}")
    public ResponseEntity<ApiResponse<Void>> deleteThread(
            @PathVariable Integer threadId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null || principal.userId() == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        Integer userId = principal.userId();
        communityThreadCommandService.deleteThread(threadId, userId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 특정 스레드에 릴레이(답글)를 생성하는 메소드
    @PostMapping("/{threadId}/replies")
    public ResponseEntity<ApiResponse<CommunityReplyCommandResponse>> createReply(
            @PathVariable Integer threadId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCommunityReplyRequest request
    ) {
        if (principal == null || principal.userId() == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        Integer userId = principal.userId();
        CommunityReplyCommandResponse response =
                communityThreadCommandService.createReply(threadId, userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 기존 릴레이(답글)를 수정하는 메소드
    @PutMapping("/replies/{replyId}")
    public ResponseEntity<ApiResponse<CommunityReplyCommandResponse>> updateReply(
            @PathVariable Integer replyId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateCommunityReplyRequest request
    ) {
        if (principal == null || principal.userId() == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        Integer userId = principal.userId();
        CommunityReplyCommandResponse response =
                communityThreadCommandService.updateReply(replyId, userId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 기존 릴레이(답글)를 삭제하는 메소드
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<ApiResponse<Void>> deleteReply(
            @PathVariable Integer replyId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null || principal.userId() == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        Integer userId = principal.userId();
        communityThreadCommandService.deleteReply(replyId, userId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

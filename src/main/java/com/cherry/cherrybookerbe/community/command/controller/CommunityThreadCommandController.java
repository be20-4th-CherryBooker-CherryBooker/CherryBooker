package com.cherry.cherrybookerbe.community.command.controller;

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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community/threads")
public class CommunityThreadCommandController {

    private final CommunityThreadCommandService communityThreadCommandService;

    public CommunityThreadCommandController(CommunityThreadCommandService communityThreadCommandService) {
        this.communityThreadCommandService = communityThreadCommandService;
    }

    // ================== 최초 스레드 ==================

    @PostMapping
    public ResponseEntity<CommunityThreadCommandResponse> createThread(
            @Valid @RequestBody CreateCommunityThreadRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(communityThreadCommandService.createThread(request));
    }

    @PutMapping("/{threadId}")
    public ResponseEntity<CommunityThreadCommandResponse> updateThread(
            @PathVariable Integer threadId,
            @Valid @RequestBody UpdateCommunityThreadRequest request
    ) {
        return ResponseEntity.ok(communityThreadCommandService.updateThread(threadId, request));
    }

    @DeleteMapping("/{threadId}")
    public ResponseEntity<Void> deleteThread(@PathVariable Integer threadId) {
        communityThreadCommandService.deleteThread(threadId);
        return ResponseEntity.noContent().build();
    }

    // ================== 릴레이(답글) ==================

    @PostMapping("/{threadId}/replies")
    public ResponseEntity<CommunityReplyCommandResponse> createReply(
            @PathVariable Integer threadId,
            @Valid @RequestBody CreateCommunityReplyRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(communityThreadCommandService.createReply(threadId, request));
    }

    @PutMapping("/replies/{replyId}")
    public ResponseEntity<CommunityReplyCommandResponse> updateReply(
            @PathVariable Integer replyId,
            @Valid @RequestBody UpdateCommunityReplyRequest request
    ) {
        return ResponseEntity.ok(communityThreadCommandService.updateReply(replyId, request));
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Integer replyId) {
        communityThreadCommandService.deleteReply(replyId);
        return ResponseEntity.noContent().build();
    }
}

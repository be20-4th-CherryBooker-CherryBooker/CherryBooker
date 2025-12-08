package com.cherry.cherrybookerbe.community.command.service;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.community.command.domain.repository.CommunityThreadRepository;
import com.cherry.cherrybookerbe.community.command.dto.request.CreateCommunityReplyRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.CreateCommunityThreadRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.UpdateCommunityReplyRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.UpdateCommunityThreadRequest;
import com.cherry.cherrybookerbe.community.command.dto.response.CommunityReplyCommandResponse;
import com.cherry.cherrybookerbe.community.command.dto.response.CommunityThreadCommandResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CommunityThreadCommandService {

    private final CommunityThreadRepository communityThreadRepository;

    public CommunityThreadCommandService(CommunityThreadRepository communityThreadRepository) {
        this.communityThreadRepository = communityThreadRepository;
    }

    // ================== 최초 스레드 ==================

    // 루트(최초) 스레드 생성
    public CommunityThreadCommandResponse createThread(CreateCommunityThreadRequest request) {
        CommunityThread thread = CommunityThread.builder()
                .parent(null)                      // 루트 스레드
                .userId(request.getUserId())
                .quoteId(request.getQuoteId())
                .build();

        CommunityThread saved = communityThreadRepository.save(thread);
        // 생성 직후 updatedAt == null -> modified(false)
        return new CommunityThreadCommandResponse(saved.getId(), saved.isUpdated());
    }

    // 루트/릴레이 공통 업데이트
    public CommunityThreadCommandResponse updateThread(Integer threadId, UpdateCommunityThreadRequest request) {
        CommunityThread thread = communityThreadRepository.findByIdAndDeletedFalse(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found: " + threadId));

        thread.updateThread(request.getQuoteId());

        return new CommunityThreadCommandResponse(thread.getId(), thread.isUpdated());
    }

    // 삭제: 루트면 자식까지, 릴레이면 자기만
    public void deleteThread(Integer threadId) {
        CommunityThread thread = communityThreadRepository.findByIdAndDeletedFalse(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found: " + threadId));

        if (thread.isRoot()) {
            // 최초 스레드 삭제 → 밑에 달린 릴레이까지 전부 삭제
            thread.markDeletedCascade();
        } else {
            // 릴레이인데 /{threadId} 로 직접 들어온 경우 보호를 위해 자기만 삭제
            thread.markDeletedOnly();
        }
    }

    // ================== 릴레이(답글) ==================

    // 릴레이 생성 (parentId = threadId)
    public CommunityReplyCommandResponse createReply(Integer parentThreadId, CreateCommunityReplyRequest request) {
        CommunityThread parent = communityThreadRepository.findByIdAndDeletedFalse(parentThreadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found: " + parentThreadId));

        CommunityThread reply = CommunityThread.builder()
                .parent(parent)                   // 부모 스레드 지정
                .userId(request.getUserId())
                .quoteId(request.getQuoteId())
                .build();

        parent.addChild(reply);
        CommunityThread saved = communityThreadRepository.save(reply);

        // 생성 직후 updatedAt == null -> updated = false
        return new CommunityReplyCommandResponse(saved.getId(), saved.isUpdated(), saved.getUpdatedAt());
    }

    public CommunityReplyCommandResponse updateReply(Integer replyId, UpdateCommunityReplyRequest request) {
        CommunityThread reply = communityThreadRepository.findByIdAndDeletedFalse(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reply not found: " + replyId));

        reply.updateThread(request.getQuoteId());

        return new CommunityReplyCommandResponse(reply.getId(), reply.isUpdated(), reply.getUpdatedAt());
    }

    public void deleteReply(Integer replyId) {
        CommunityThread reply = communityThreadRepository.findByIdAndDeletedFalse(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reply not found: " + replyId));

        // 릴레이만 삭제 (루트 삭제 로직은 deleteThread 에서 처리)
        reply.markDeletedOnly();
    }
}

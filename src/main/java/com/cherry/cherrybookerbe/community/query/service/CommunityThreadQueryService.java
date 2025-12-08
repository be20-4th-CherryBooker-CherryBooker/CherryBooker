package com.cherry.cherrybookerbe.community.query.service;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.community.command.domain.repository.CommunityThreadRepository;
import com.cherry.cherrybookerbe.community.query.dto.CommunityReplyResponse;
import com.cherry.cherrybookerbe.community.query.dto.CommunityThreadDetailResponse;
import com.cherry.cherrybookerbe.community.query.dto.CommunityThreadSummaryResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class CommunityThreadQueryService {

    private final CommunityThreadRepository communityThreadRepository;

    public CommunityThreadQueryService(CommunityThreadRepository communityThreadRepository) {
        this.communityThreadRepository = communityThreadRepository;
    }

    /**
     * 스레드 목록 조회: 삭제되지 않은 루트 스레드만 (parent == null)
     */
    public List<CommunityThreadSummaryResponse> getThreadList() {
        return communityThreadRepository
                .findByDeletedFalseAndParentIsNullOrderByCreatedAtDesc()
                .stream()
                .map(this::mapThreadSummary)
                .collect(Collectors.toList());
    }

    /**
     * 스레드 상세 조회: 루트/릴레이 구분 없이, 해당 스레드 + 바로 아래 자식 릴레이들
     */
    public CommunityThreadDetailResponse getThreadDetail(Integer threadId) {
        CommunityThread thread = communityThreadRepository.findByIdAndDeletedFalse(threadId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Thread not found: " + threadId));

        // parent == threadId 인 자식 스레드들 = 릴레이
        List<CommunityReplyResponse> replies = thread.getChildren().stream()
                .map(this::mapReply)
                .collect(Collectors.toList());

        return mapThreadDetail(thread, replies);
    }

    // ====== 매핑 메서드 ======

    private CommunityThreadSummaryResponse mapThreadSummary(CommunityThread thread) {
        return new CommunityThreadSummaryResponse(
                thread.getId(),
                thread.getUserId(),
//                user.getNickname(),
                thread.getQuoteId(),
//                quote.getBookTitle(),
//                quote.getText(),
                thread.getCreatedAt(),
                thread.getUpdatedAt(),
                thread.isUpdated(),
                thread.isDeleted(),
                thread.getReportCount()
        );
    }

    private CommunityThreadDetailResponse mapThreadDetail(CommunityThread thread,
                                                          List<CommunityReplyResponse> replies) {
        return new CommunityThreadDetailResponse(
                thread.getId(),
                thread.getUserId(),
                thread.getQuoteId(),
                thread.getCreatedAt(),
                thread.getUpdatedAt(),
                thread.isUpdated(),
                thread.isDeleted(),
                thread.getReportCount(),
                replies
        );
    }

    private CommunityReplyResponse mapReply(CommunityThread reply) {
        return new CommunityReplyResponse(
                reply.getId(),
                reply.getUserId(),
                reply.getQuoteId(),
                reply.getCreatedAt(),
                reply.getUpdatedAt(),
                reply.isUpdated(),
                reply.isDeleted(),
                reply.getReportCount()
        );
    }
}

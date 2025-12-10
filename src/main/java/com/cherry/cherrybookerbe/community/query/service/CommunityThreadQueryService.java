package com.cherry.cherrybookerbe.community.query.service;

import com.cherry.cherrybookerbe.common.dto.Pagination;
import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.community.command.domain.repository.CommunityThreadRepository;
import com.cherry.cherrybookerbe.community.query.dto.CommunityReplyResponse;
import com.cherry.cherrybookerbe.community.query.dto.CommunityThreadDetailResponse;
import com.cherry.cherrybookerbe.community.query.dto.CommunityThreadListResponse;
import com.cherry.cherrybookerbe.community.query.dto.CommunityThreadSummaryResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import com.cherry.cherrybookerbe.quote.query.repository.QuoteQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class CommunityThreadQueryService {

    private final CommunityThreadRepository communityThreadRepository;
    private final QuoteQueryRepository quoteRepository;

    public CommunityThreadQueryService(CommunityThreadRepository communityThreadRepository,
                                       QuoteQueryRepository quoteRepository) {
        this.communityThreadRepository = communityThreadRepository;
        this.quoteRepository = quoteRepository;
    }

    /** 스레드 목록 조회 (페이징) */
    public CommunityThreadListResponse getThreadList(int page, int size) {

        // page < 0 방어
        int pageIndex = Math.max(page, 0);
        int pageSize = size <= 0 ? 10 : size;   // 기본 10개

        Pageable pageable = PageRequest.of(
                pageIndex,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")   // 최신 스레드 먼저
        );

        Page<CommunityThread> threadPage =
                communityThreadRepository.findByDeletedFalseAndParentIsNull(pageable);

        List<CommunityThread> threads = threadPage.getContent();

        // 글귀 ID 모아서 한 번에 조회
        List<Long> quoteIds = threads.stream()
                .map(CommunityThread::getQuoteId)
                .map(Integer::longValue)
                .distinct()
                .toList();

        Map<Long, Quote> quoteMap = quoteRepository.findAllById(quoteIds)
                .stream()
                .collect(Collectors.toMap(Quote::getQuoteId, q -> q));

        List<CommunityThreadSummaryResponse> summaries = threads.stream()
                .map(thread -> mapThreadSummary(thread, quoteMap))
                .toList();

        Pagination pagination = Pagination.builder()
                .currentPage(threadPage.getNumber())
                .totalPages(threadPage.getTotalPages())
                .totalItems(threadPage.getTotalElements())
                .build();

        return new CommunityThreadListResponse(summaries, pagination);
    }


    /** 스레드 상세 조회 */
    public CommunityThreadDetailResponse getThreadDetail(Integer threadId) {
        CommunityThread thread = communityThreadRepository.findByIdAndDeletedFalse(threadId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Thread not found: " + threadId));

        // 루트 + 자식 릴레이들의 quoteId 한번에 수집
        List<CommunityThread> all = new ArrayList<>();
        all.add(thread);
        all.addAll(thread.getChildren());

        List<Long> quoteIds = all.stream()
                .map(CommunityThread::getQuoteId)
                .map(Integer::longValue)
                .distinct()
                .toList();

        Map<Long, Quote> quoteMap = quoteRepository.findAllById(quoteIds)
                .stream()
                .collect(Collectors.toMap(Quote::getQuoteId, q -> q));

        String threadContent = getQuoteContent(quoteMap, thread.getQuoteId());

        List<CommunityReplyResponse> replies = thread.getChildren().stream()
                .map(child -> mapReply(child, quoteMap))
                .toList();

        return mapThreadDetail(thread, threadContent, replies);
    }

    /* ====== 내부 헬퍼 메서드들 ====== */

    private String getQuoteContent(Map<Long, Quote> map, Integer quoteId) {
        if (quoteId == null) return null;
        Quote q = map.get(quoteId.longValue());
        return q != null ? q.getContent() : null;
    }

    private CommunityThreadSummaryResponse mapThreadSummary(CommunityThread thread,
                                                            Map<Long, Quote> quoteMap) {
        return new CommunityThreadSummaryResponse(
                thread.getId(),
                thread.getUserId(),
                thread.getQuoteId(),
                getQuoteContent(quoteMap, thread.getQuoteId()),
                thread.getCreatedAt(),
                thread.getUpdatedAt(),
                thread.isUpdated(),
                thread.isDeleted(),
                thread.getReportCount()
        );
    }

    private CommunityThreadDetailResponse mapThreadDetail(CommunityThread thread,
                                                          String quoteContent,
                                                          List<CommunityReplyResponse> replies) {
        return new CommunityThreadDetailResponse(
                thread.getId(),
                thread.getUserId(),
                thread.getQuoteId(),
                quoteContent,
                thread.getCreatedAt(),
                thread.getUpdatedAt(),
                thread.isUpdated(),
                thread.isDeleted(),
                thread.getReportCount(),
                replies
        );
    }

    private CommunityReplyResponse mapReply(CommunityThread reply,
                                            Map<Long, Quote> quoteMap) {
        return new CommunityReplyResponse(
                reply.getId(),
                reply.getUserId(),
                reply.getQuoteId(),
                getQuoteContent(quoteMap, reply.getQuoteId()),
                reply.getCreatedAt(),
                reply.getUpdatedAt(),
                reply.isUpdated(),
                reply.isDeleted(),
                reply.getReportCount()
        );
    }
}



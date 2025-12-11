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
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import com.cherry.cherrybookerbe.user.command.repository.UserRepository;
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
    private final UserRepository userRepository;

    public CommunityThreadQueryService(CommunityThreadRepository communityThreadRepository,
                                       QuoteQueryRepository quoteRepository, UserRepository userRepository) {
        this.communityThreadRepository = communityThreadRepository;
        this.quoteRepository = quoteRepository;
        this.userRepository = userRepository;
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

        // 1) 글귀 ID 모아서 한 번에 조회 (기존)
        List<Long> quoteIds = threads.stream()
                .map(CommunityThread::getQuoteId)
                .map(Integer::longValue)
                .distinct()
                .toList();

        Map<Long, Quote> quoteMap = quoteRepository.findAllById(quoteIds)
                .stream()
                .collect(Collectors.toMap(Quote::getQuoteId, q -> q));

        // 작성자 userId 들 모아서 한 번에 조회
        List<Integer> userIds = threads.stream()
                .map(CommunityThread::getUserId)
                .distinct()
                .toList();

        Map<Integer, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        // DTO 매핑할 때 닉네임도 함께 넣기
        List<CommunityThreadSummaryResponse> summaries = threads.stream()
                .map(thread -> mapThreadSummary(thread, quoteMap, userMap))
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

        // 글귀 조회
        List<Long> quoteIds = all.stream()
                .map(CommunityThread::getQuoteId)
                .map(Integer::longValue)
                .distinct()
                .toList();

        Map<Long, Quote> quoteMap = quoteRepository.findAllById(quoteIds)
                .stream()
                .collect(Collectors.toMap(Quote::getQuoteId, q -> q));

        // 작성자 userId 모아서 한번에 조회
        List<Integer> userIds = all.stream()
                .map(CommunityThread::getUserId)
                .distinct()
                .toList();

        Map<Integer, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        String threadContent = getQuoteContent(quoteMap, thread.getQuoteId());
        String nickname = getUserNickname(userMap, thread.getUserId());

        List<CommunityReplyResponse> replies = thread.getChildren().stream()
                .map(child -> mapReply(child, quoteMap, userMap))
                .toList();

        return mapThreadDetail(thread, nickname, threadContent, replies);
    }


    /* ====== 내부 헬퍼 메서드들 ====== */

    private String getUserNickname(Map<Integer, User> userMap, Integer userId) {
        if (userId == null) return null;
        User u = userMap.get(userId);
        return u != null ? u.getUserNickname() : "알 수 없음";
    }

    private String getQuoteContent(Map<Long, Quote> map, Integer quoteId) {
        if (quoteId == null) return null;
        Quote q = map.get(quoteId.longValue());
        return q != null ? q.getContent() : null;
    }

    private CommunityThreadSummaryResponse mapThreadSummary(CommunityThread thread,
                                                            Map<Long, Quote> quoteMap,
                                                            Map<Integer, User> userMap) {

        String content = getQuoteContent(quoteMap, thread.getQuoteId());

        String nickname = getUserNickname(userMap, thread.getUserId());

        return new CommunityThreadSummaryResponse(
                thread.getId(),
                thread.getUserId(),
                nickname,
                thread.getQuoteId(),
                content,
                thread.getCreatedAt(),
                thread.getUpdatedAt(),
                thread.isUpdated(),
                thread.isDeleted(),
                thread.getReportCount()
        );
    }


    private CommunityThreadDetailResponse mapThreadDetail(CommunityThread thread,
                                                          String userNickname,
                                                          String quoteContent,
                                                          List<CommunityReplyResponse> replies) {
        return new CommunityThreadDetailResponse(
                thread.getId(),
                thread.getUserId(),
                userNickname,
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
                                            Map<Long, Quote> quoteMap,
                                            Map<Integer, User> userMap) {
        User author = userMap.get(reply.getUserId());
        String nickname = author != null ? author.getUserNickname() : "알 수 없음";

        return new CommunityReplyResponse(
                reply.getId(),
                reply.getUserId(),
                nickname,
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
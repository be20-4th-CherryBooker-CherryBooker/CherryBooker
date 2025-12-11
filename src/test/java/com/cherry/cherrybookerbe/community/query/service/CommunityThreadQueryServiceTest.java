package com.cherry.cherrybookerbe.community.query.service;

import com.cherry.cherrybookerbe.common.dto.Pagination;
import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.community.command.domain.repository.CommunityThreadRepository;
import com.cherry.cherrybookerbe.community.query.dto.CommunityReplyResponse;
import com.cherry.cherrybookerbe.community.query.dto.CommunityThreadDetailResponse;
import com.cherry.cherrybookerbe.community.query.dto.CommunityThreadListResponse;
import com.cherry.cherrybookerbe.community.query.dto.CommunityThreadSummaryResponse;
import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import com.cherry.cherrybookerbe.quote.query.repository.QuoteQueryRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityThreadQueryServiceTest {

    @Mock
    private CommunityThreadRepository communityThreadRepository;

    @Mock
    private QuoteQueryRepository quoteRepository;

    @InjectMocks
    private CommunityThreadQueryService communityThreadQueryService;

    private CommunityThread createThreadEntity(Integer id,
                                               Integer userId,
                                               Integer quoteId,
                                               boolean deleted,
                                               int reportCount,
                                               boolean updated,
                                               LocalDateTime createdAt,
                                               LocalDateTime updatedAtOverride) {
        CommunityThread thread = CommunityThread.builder()
                .parent(null)
                .userId(userId)
                .quoteId(quoteId)
                .build();

        if (id != null) {
            ReflectionTestUtils.setField(thread, "id", id);
        }

        ReflectionTestUtils.setField(thread, "createdAt", createdAt);
        ReflectionTestUtils.setField(thread, "updatedAt",
                updated ? (updatedAtOverride != null ? updatedAtOverride : createdAt.plusMinutes(1)) : null);
        ReflectionTestUtils.setField(thread, "deleted", deleted);
        ReflectionTestUtils.setField(thread, "reportCount", reportCount);

        return thread;
    }

    private CommunityThread createReplyEntity(Integer id,
                                              CommunityThread parent,
                                              Integer userId,
                                              Integer quoteId,
                                              boolean deleted,
                                              boolean updated,
                                              LocalDateTime createdAt) {
        CommunityThread reply = CommunityThread.builder()
                .parent(parent)
                .userId(userId)
                .quoteId(quoteId)
                .build();

        if (id != null) {
            ReflectionTestUtils.setField(reply, "id", id);
        }

        ReflectionTestUtils.setField(reply, "createdAt", createdAt);
        ReflectionTestUtils.setField(reply, "updatedAt",
                updated ? createdAt.plusMinutes(1) : null);
        ReflectionTestUtils.setField(reply, "deleted", deleted);

        if (parent != null) {
            parent.addChild(reply);
        }

        return reply;
    }

    @Test
    @DisplayName("CMT-004/CMT-006: 스레드 목록 조회 시 삭제되지 않은 루트 스레드만 요약 + 페이징 정보로 반환된다")
    void getThreadList_returnsPagedSummaryOfRootThreads() {
        // given
        LocalDateTime now = LocalDateTime.now();

        CommunityThread thread1 = createThreadEntity(
                1, 1, 10, false, 0, false,
                now.minusMinutes(10), null
        );
        CommunityThread thread2 = createThreadEntity(
                2, 2, 20, false, 1, true,
                now.minusMinutes(5), now.minusMinutes(3)
        );

        // PageImpl 로 가짜 페이지 객체 구성
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommunityThread> page = new PageImpl<>(
                List.of(thread2, thread1), // createdAt DESC 정렬이라고 가정
                pageable,
                2
        );

        when(communityThreadRepository.findByDeletedFalseAndParentIsNull(any(Pageable.class)))
                .thenReturn(page);

        // Quote 엔티티는 Mockito 로 목 객체 생성 (ID, content 매핑만 사용)
        Quote quote1 = mock(Quote.class);
        Quote quote2 = mock(Quote.class);
        when(quote1.getQuoteId()).thenReturn(10L);
        when(quote2.getQuoteId()).thenReturn(20L);
        when(quote1.getContent()).thenReturn("QUOTE-10");
        when(quote2.getContent()).thenReturn("QUOTE-20");

        // 어떤 ID 리스트가 와도 위 두 개를 반환하도록 설정
        when(quoteRepository.findAllById(any()))
                .thenReturn(List.of(quote1, quote2));

        // when
        CommunityThreadListResponse result = communityThreadQueryService.getThreadList(0, 10);

        // then: 스레드 요약 정보
        assertThat(result.getThreads()).hasSize(2);

        CommunityThreadSummaryResponse first = result.getThreads().get(0);
        CommunityThreadSummaryResponse second = result.getThreads().get(1);

        // 첫 번째 요소가 thread2 인지 확인 (createdAt desc 가정)
        assertThat(first.getThreadId()).isEqualTo(2);
        assertThat(first.getUserId()).isEqualTo(2);
        assertThat(first.getQuoteId()).isEqualTo(20);
        assertThat(first.isUpdated()).isTrue();
        assertThat(first.isDeleted()).isFalse();
        assertThat(first.getReportCount()).isEqualTo(1);
        assertThat(first.getQuoteContent()).isEqualTo("QUOTE-20");

        // 두 번째 요소
        assertThat(second.getThreadId()).isEqualTo(1);
        assertThat(second.getUserId()).isEqualTo(1);
        assertThat(second.getQuoteId()).isEqualTo(10);
        assertThat(second.isUpdated()).isFalse();
        assertThat(second.isDeleted()).isFalse();
        assertThat(second.getReportCount()).isEqualTo(0);
        assertThat(second.getQuoteContent()).isEqualTo("QUOTE-10");

        // then: 페이징 정보
        Pagination pagination = result.getPagination();
        assertThat(pagination.getCurrentPage()).isEqualTo(0);
        assertThat(pagination.getTotalPages()).isEqualTo(1);
        assertThat(pagination.getTotalItems()).isEqualTo(2L);
    }

    @Test
    @DisplayName("CMT-005/CMT-007/CMT-009: 스레드 상세 조회 시 현재 스레드와 자식 릴레이 목록이 함께 조회된다")
    void getThreadDetail_returnsThreadAndChildrenReplies() {
        // given
        LocalDateTime now = LocalDateTime.now();

        CommunityThread root = createThreadEntity(
                1, 1, 10, false, 0, true,
                now.minusMinutes(20), now.minusMinutes(10)
        );

        CommunityThread reply1 = createReplyEntity(
                2, root, 2, 20, false, true,
                now.minusMinutes(9)
        );
        CommunityThread reply2 = createReplyEntity(
                3, root, 3, 30, true, false, // 삭제된 답변 (CMT-009)
                now.minusMinutes(8)
        );

        when(communityThreadRepository.findByIdAndDeletedFalse(1))
                .thenReturn(Optional.of(root));

        // quote 내용은 이 테스트에서 중요하지 않으므로 빈 리스트 반환
        when(quoteRepository.findAllById(any()))
                .thenReturn(Collections.emptyList());

        // when
        CommunityThreadDetailResponse result = communityThreadQueryService.getThreadDetail(1);

        // then: 루트 스레드 정보
        assertThat(result.getThreadId()).isEqualTo(1);
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getQuoteId()).isEqualTo(10);
        assertThat(result.isUpdated()).isTrue();
        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getReportCount()).isEqualTo(0);

        // then: 릴레이 목록
        List<CommunityReplyResponse> replies = result.getReplies();
        assertThat(replies).hasSize(2);

        CommunityReplyResponse r1 = replies.stream()
                .filter(r -> r.getReplyId().equals(2))
                .findFirst()
                .orElseThrow();

        CommunityReplyResponse r2 = replies.stream()
                .filter(r -> r.getReplyId().equals(3))
                .findFirst()
                .orElseThrow();

        assertThat(r1.getUserId()).isEqualTo(2);
        assertThat(r1.getQuoteId()).isEqualTo(20);
        assertThat(r1.isDeleted()).isFalse();
        assertThat(r1.isUpdated()).isTrue();

        // 삭제된 답변은 deleted=true -> 프론트에서 "이 글귀는 삭제되었습니다"로 대체 표시(CMT-009)
        assertThat(r2.getUserId()).isEqualTo(3);
        assertThat(r2.getQuoteId()).isEqualTo(30);
        assertThat(r2.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("스레드 상세 조회 시 대상 스레드가 없으면 404를 던진다")
    void getThreadDetail_notFound() {
        // given
        when(communityThreadRepository.findByIdAndDeletedFalse(999))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> communityThreadQueryService.getThreadDetail(999))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}

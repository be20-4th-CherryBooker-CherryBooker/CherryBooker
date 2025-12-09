package com.cherry.cherrybookerbe.community.command.service;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.community.command.domain.repository.CommunityThreadRepository;
import com.cherry.cherrybookerbe.community.command.dto.request.CreateCommunityReplyRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.CreateCommunityThreadRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.UpdateCommunityReplyRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.UpdateCommunityThreadRequest;
import com.cherry.cherrybookerbe.community.command.dto.response.CommunityReplyCommandResponse;
import com.cherry.cherrybookerbe.community.command.dto.response.CommunityThreadCommandResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityThreadCommandServiceTest {

    @Mock
    private CommunityThreadRepository communityThreadRepository;

    @InjectMocks
    private CommunityThreadCommandService communityThreadCommandService;

    private CommunityThread createThreadEntity(Integer id,
                                               Integer userId,
                                               Integer quoteId,
                                               boolean deleted,
                                               int reportCount,
                                               boolean updated) {
        CommunityThread thread = CommunityThread.builder()
                .parent(null)
                .userId(userId)
                .quoteId(quoteId)
                .build();

        if (id != null) {
            ReflectionTestUtils.setField(thread, "id", id);
        }

        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(10);
        LocalDateTime updatedAt = updated ? LocalDateTime.now() : null;

        ReflectionTestUtils.setField(thread, "createdAt", createdAt);
        ReflectionTestUtils.setField(thread, "updatedAt", updatedAt);
        ReflectionTestUtils.setField(thread, "deleted", deleted);
        ReflectionTestUtils.setField(thread, "reportCount", reportCount);

        return thread;
    }

    private CommunityThread createReplyEntity(Integer id,
                                              CommunityThread parent,
                                              Integer userId,
                                              Integer quoteId,
                                              boolean deleted,
                                              boolean updated) {
        CommunityThread reply = CommunityThread.builder()
                .parent(parent)
                .userId(userId)
                .quoteId(quoteId)
                .build();

        if (id != null) {
            ReflectionTestUtils.setField(reply, "id", id);
        }

        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime updatedAt = updated ? LocalDateTime.now() : null;

        ReflectionTestUtils.setField(reply, "createdAt", createdAt);
        ReflectionTestUtils.setField(reply, "updatedAt", updatedAt);
        ReflectionTestUtils.setField(reply, "deleted", deleted);

        if (parent != null) {
            parent.addChild(reply);
        }

        return reply;
    }

    @Test
    @DisplayName("CMT-001: 루트 스레드 생성 - 사용자는 자신의 글귀를 스레드로 등록할 수 있다")
    void createThread_createsRootThread() {
        // given
        CreateCommunityThreadRequest request = new CreateCommunityThreadRequest();
        ReflectionTestUtils.setField(request, "userId", 1);
        ReflectionTestUtils.setField(request, "quoteId", 10);

        CommunityThread saved = createThreadEntity(100, 1, 10, false, 0, false);

        ArgumentCaptor<CommunityThread> captor = ArgumentCaptor.forClass(CommunityThread.class);
        when(communityThreadRepository.save(any(CommunityThread.class))).thenReturn(saved);

        // when
        CommunityThreadCommandResponse response = communityThreadCommandService.createThread(request);

        // then
        verify(communityThreadRepository).save(captor.capture());
        CommunityThread toSave = captor.getValue();

        // 루트 스레드인지 확인
        assertThat(toSave.getParent()).isNull();
        assertThat(toSave.getUserId()).isEqualTo(1);
        assertThat(toSave.getQuoteId()).isEqualTo(10);

        // 응답 검증
        assertThat(response.getThreadId()).isEqualTo(100);
        // 생성 직후에는 updated=false 여야 "수정됨"이 안 붙음
        assertThat(response.isUpdated()).isFalse();
    }

    @Test
    @DisplayName("CMT-002: 스레드 글귀 수정 - 수정 후 updated 플래그가 true")
    void updateThread_updatesQuote() {
        // given
        CommunityThread existing = createThreadEntity(100, 1, 10, false, 0, true);
        when(communityThreadRepository.findByIdAndDeletedFalse(100))
                .thenReturn(Optional.of(existing));

        UpdateCommunityThreadRequest request = new UpdateCommunityThreadRequest();
        ReflectionTestUtils.setField(request, "quoteId", 20);

        // when
        CommunityThreadCommandResponse response = communityThreadCommandService.updateThread(100, request);

        // then
        assertThat(existing.getQuoteId()).isEqualTo(20);
        assertThat(response.getThreadId()).isEqualTo(100);
        // updatedAt 이 존재한다고 가정 → "수정됨" 표시용
        assertThat(response.isUpdated()).isTrue();
    }

    @Test
    @DisplayName("CMT-002: 존재하지 않는 스레드를 수정하려 하면 404 응답을 던진다")
    void updateThread_notFound() {
        // given
        when(communityThreadRepository.findByIdAndDeletedFalse(999))
                .thenReturn(Optional.empty());

        UpdateCommunityThreadRequest request = new UpdateCommunityThreadRequest();
        ReflectionTestUtils.setField(request, "quoteId", 20);

        // when & then
        assertThatThrownBy(() -> communityThreadCommandService.updateThread(999, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("CMT-003: 루트 스레드 삭제 시 자신과 모든 릴레이가 소프트 삭제된다")
    void deleteThread_rootCascade() {
        // given
        CommunityThread root = createThreadEntity(1, 1, 10, false, 0, false);
        CommunityThread child1 = createReplyEntity(2, root, 2, 11, false, false);
        CommunityThread child2 = createReplyEntity(3, root, 3, 12, false, false);

        when(communityThreadRepository.findByIdAndDeletedFalse(1))
                .thenReturn(Optional.of(root));

        // when
        communityThreadCommandService.deleteThread(1);

        // then
        assertThat(root.isDeleted()).isTrue();
        assertThat(child1.isDeleted()).isTrue();
        assertThat(child2.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("CMT-003: 루트가 아닌 스레드를 deleteThread 로 삭제하면 자기 자신만 소프트 삭제된다")
    void deleteThread_nonRootDeletesOnlySelf() {
        // given
        CommunityThread root = createThreadEntity(1, 1, 10, false, 0, false);
        CommunityThread child = createReplyEntity(2, root, 2, 11, false, false);

        when(communityThreadRepository.findByIdAndDeletedFalse(2))
                .thenReturn(Optional.of(child));

        // when
        communityThreadCommandService.deleteThread(2);

        // then
        assertThat(child.isDeleted()).isTrue();
        assertThat(root.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("CMT-007: 특정 스레드에 자신의 글귀로 릴레이(답변)를 등록할 수 있다")
    void createReply_createsChildThread() {
        // given
        CommunityThread parent = createThreadEntity(1, 1, 10, false, 0, false);
        when(communityThreadRepository.findByIdAndDeletedFalse(1))
                .thenReturn(Optional.of(parent));

        CreateCommunityReplyRequest request = new CreateCommunityReplyRequest();
        ReflectionTestUtils.setField(request, "userId", 2);
        ReflectionTestUtils.setField(request, "quoteId", 20);

        when(communityThreadRepository.save(any(CommunityThread.class)))
                .thenAnswer(invocation -> {
                    CommunityThread reply = invocation.getArgument(0, CommunityThread.class);
                    ReflectionTestUtils.setField(reply, "id", 1000);
                    ReflectionTestUtils.setField(reply, "createdAt", LocalDateTime.now());
                    return reply;
                });

        // when
        CommunityReplyCommandResponse response = communityThreadCommandService.createReply(1, request);

        // then
        verify(communityThreadRepository).save(any(CommunityThread.class));

        assertThat(response.getReplyId()).isEqualTo(1000);
        assertThat(response.isUpdated()).isFalse();

        // parent-children 연관관계 검증
        assertThat(parent.getChildren())
                .extracting(CommunityThread::getId)
                .contains(1000);
    }

    @Test
    @DisplayName("CMT-008: 사용자는 자신의 답변(릴레이)을 다른 글귀로 수정할 수 있다")
    void updateReply_updatesQuote() {
        // given
        CommunityThread parent = createThreadEntity(1, 1, 10, false, 0, false);
        CommunityThread reply = createReplyEntity(2, parent, 2, 20, false, true);

        when(communityThreadRepository.findByIdAndDeletedFalse(2))
                .thenReturn(Optional.of(reply));

        UpdateCommunityReplyRequest request = new UpdateCommunityReplyRequest();
        ReflectionTestUtils.setField(request, "quoteId", 30);

        // when
        CommunityReplyCommandResponse response = communityThreadCommandService.updateReply(2, request);

        // then
        assertThat(reply.getQuoteId()).isEqualTo(30);
        assertThat(response.getReplyId()).isEqualTo(2);
        assertThat(response.isUpdated()).isTrue();
    }

    @Test
    @DisplayName("CMT-009: 사용자가 등록한 답변을 삭제하면 해당 답변만 소프트 삭제된다")
    void deleteReply_softDeleteOnlyReply() {
        // given
        CommunityThread parent = createThreadEntity(1, 1, 10, false, 0, false);
        CommunityThread reply = createReplyEntity(2, parent, 2, 20, false, false);

        when(communityThreadRepository.findByIdAndDeletedFalse(2))
                .thenReturn(Optional.of(reply));

        // when
        communityThreadCommandService.deleteReply(2);

        // then
        assertThat(reply.isDeleted()).isTrue();
        assertThat(parent.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("CMT-009: 존재하지 않는 답변 삭제 시 404를 던진다")
    void deleteReply_notFound() {
        // given
        when(communityThreadRepository.findByIdAndDeletedFalse(999))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> communityThreadCommandService.deleteReply(999))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}

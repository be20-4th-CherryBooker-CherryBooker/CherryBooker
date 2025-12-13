package com.cherry.cherrybookerbe.community.command.service;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.community.command.domain.repository.CommunityThreadRepository;
import com.cherry.cherrybookerbe.community.command.dto.request.CreateCommunityReplyRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.CreateCommunityThreadRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.UpdateCommunityReplyRequest;
import com.cherry.cherrybookerbe.community.command.dto.request.UpdateCommunityThreadRequest;
import com.cherry.cherrybookerbe.community.command.dto.response.CommunityReplyCommandResponse;
import com.cherry.cherrybookerbe.community.command.dto.response.CommunityThreadCommandResponse;
import com.cherry.cherrybookerbe.notification.command.event.ThreadReplyCreatedEvent;
import com.cherry.cherrybookerbe.user.command.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@Transactional
public class CommunityThreadCommandService {

    private final CommunityThreadRepository communityThreadRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CommunityThreadCommandService(
            CommunityThreadRepository communityThreadRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher) {
        this.communityThreadRepository = communityThreadRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    // ================== 최초 스레드 ==================

    // 루트(최초) 스레드 생성
    public CommunityThreadCommandResponse createThread(Integer userId,
                                                       CreateCommunityThreadRequest request) {
        CommunityThread thread = CommunityThread.builder()
                .parent(null)
                .userId(userId)
                .quoteId(request.getQuoteId())
                .build();

        CommunityThread saved = communityThreadRepository.save(thread);
        // 생성 직후 updatedAt == null -> modified(false)
        return new CommunityThreadCommandResponse(saved.getId(), saved.isUpdated());
    }

    // 루트/릴레이 공통 업데이트
    public CommunityThreadCommandResponse updateThread(Integer threadId,
                                                       Integer userId,
                                                       UpdateCommunityThreadRequest request) {
        CommunityThread thread = communityThreadRepository.findByIdAndDeletedFalse(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found: " + threadId));

        if (!thread.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 스레드만 수정할 수 있습니다.");
        }

        thread.updateThread(request.getQuoteId());
        return new CommunityThreadCommandResponse(thread.getId(), thread.isUpdated());
    }

    // 삭제: 루트면 자식까지, 릴레이면 자기만
    public void deleteThread(Integer threadId, Integer userId) {
        CommunityThread thread = communityThreadRepository.findByIdAndDeletedFalse(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found: " + threadId));

        if (!thread.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 스레드만 삭제할 수 있습니다.");
        }

        if (thread.isRoot()) {
            thread.markDeletedCascade();
        } else {
            thread.markDeletedOnly();
        }
    }

    // ================== 릴레이(답글) ==================

    // 릴레이 생성 (parentId = threadId)
    public CommunityReplyCommandResponse createReply(Integer parentThreadId,
                                                     Integer userId,
                                                     CreateCommunityReplyRequest request) {
        CommunityThread parent = communityThreadRepository.findByIdAndDeletedFalse(parentThreadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found: " + parentThreadId));

        CommunityThread reply = CommunityThread.builder()
                .parent(parent)
                .userId(userId)
                .quoteId(request.getQuoteId())
                .build();

        parent.addChild(reply);
        CommunityThread saved = communityThreadRepository.save(reply);

        // ====== 알림용 이벤트 발행(추가) ======
        CommunityThread root = findRoot(parent);
        Integer ownerUserId = root.getUserId();

        String writerNickname = userRepository.findById(userId)
                .map(u -> u.getUserNickname())
                .filter(s -> s != null && !s.isBlank())
                .orElse("사용자" + userId);

        // 자기 글에 자기 답글이면 보통 알림 제외(정책)
        if (!ownerUserId.equals(userId)) {
            ThreadReplyCreatedEvent ev = new ThreadReplyCreatedEvent(
                    root.getId(),
                    ownerUserId,
                    userId,
                    writerNickname
            );
            log.info("[COMMUNITY] publish ThreadReplyCreatedEvent={}", ev);
            eventPublisher.publishEvent(ev);
        }
        // =====================================

        return new CommunityReplyCommandResponse(saved.getId(), saved.isUpdated(), saved.getUpdatedAt());
    }

    public CommunityReplyCommandResponse updateReply(Integer replyId,
                                                     Integer userId,
                                                     UpdateCommunityReplyRequest request) {
        CommunityThread reply = communityThreadRepository.findByIdAndDeletedFalse(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reply not found: " + replyId));

        if (!reply.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 답글만 수정할 수 있습니다.");
        }

        reply.updateThread(request.getQuoteId());
        return new CommunityReplyCommandResponse(reply.getId(), reply.isUpdated(), reply.getUpdatedAt());
    }

    public void deleteReply(Integer replyId, Integer userId) {
        CommunityThread reply = communityThreadRepository.findByIdAndDeletedFalse(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reply not found: " + replyId));

        if (!reply.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 답글만 삭제할 수 있습니다.");
        }

        reply.markDeletedOnly();
    }

    private CommunityThread findRoot(CommunityThread node) {
        CommunityThread cur = node;
        while (cur.getParent() != null) {
            cur = cur.getParent();
        }
        return cur;
    }
}

package com.cherry.cherrybookerbe.report.query;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityReply;
import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import com.cherry.cherrybookerbe.report.query.dto.ReportPendingResponse;
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// 5회 이상 신고 받은 목록 조회
/*
@Service
@Transactional(readOnly = true)
public class ReportQueryService {
     /*
    private final ReportQueryRepository reportQueryRepository;
    private final CommunityThreadRepository threadRepository;
    private final CommunityReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final QuoteRepository quoteRepository;


    public ReportQueryService(
            ReportQueryRepository reportQueryRepository,
            QuoteRepository quoteRepository) {
        this.reportQueryRepository = reportQueryRepository;
        this.quoteRepository = quoteRepository;
    }

    // 관리자 신고 목록 조회. 5회 이상 + PENDING
    public List<ReportPendingResponse> getPendingReportsForAdmin() {
        List<ReportPendingResponse> result = new ArrayList<>();

        List<Long> pendingThreadIds
                = reportQueryRepository.findPendingThreadIdsReportedOverFive();

        List<Long> pendingReportIds
                = reportQueryRepository.findPendingReplyIdsReportedOverFive();

        for (Long threadId : pendingThreadIds) {
            CommunityThread thread = threadRepository.findById(pendingThreadIds)
                    .orElseThrow(() -> new IllegalAccessException("게시글 없음"));

            Quote quote = quoteRepository.findById(thread.getQuoteId().longValue())
                    .orElseThrow(() -> new IllegalArgumentException("글귀 없음"));

            Long reportedUserId = thread.getUserId().longValue();

            User reportedUser = userRepository.findById(reportedUserId)
                    .orElseThrow(() -> new IllegalAccessException("유저 없음"));


            ReportPendingResponse reportPendingResponse = new ReportPendingResponse(
                    reportedUser.getUserId().longValue(),
                    thread.getId().longValue(),
                    null,
                    thread.getCreatedAt(),
                    thread.getReportCount(),
                    reportedUser.getUserNickname(),
                    quote.getContent()
            );
            result.add(reportPendingResponse);
        }
        for (Long threadId : pendingReportIds) {
            CommunityReply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new IllegalAccessException("댓글 없음"));

            Quote quote = quoteRepository.findById(reply.getQuoteId().longValue())
                    .orElseThrow(() -> new IllegalArgumentException("글귀 없음"));

            Long reportedUserId = reply.getUserId().longValue();
            User reportedUser = userRepository.findById(reportedUserId)
                    .orElseThrow(() -> new IllegalAccessException("유저 없음"));

            ReportPendingResponse reportPendingResponse = new ReportPendingResponse(
                    reportedUser.getUserId().longValue(),
                    null,
                    reply.getId().longValue(),
                    reply.getCreatedAt(),
                    reply.getReportCount(),
                    reportedUser.getUserNickname(),
                    quote.getContent()
            );
            result.add(reportPendingResponse);
        }
    }
}
*/

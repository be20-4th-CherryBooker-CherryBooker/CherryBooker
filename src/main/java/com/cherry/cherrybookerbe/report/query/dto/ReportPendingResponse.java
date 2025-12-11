package com.cherry.cherrybookerbe.report.query.dto;

import com.cherry.cherrybookerbe.report.domain.ReportStatus;
import lombok.Getter;

import java.time.LocalDateTime;

// 신고 처리 목록 조회 dto
@Getter
public class ReportPendingResponse {

    private Long reportId;       // 🔥 추가됨
    private Long reportedUserId;
    private String targetNickname;

    private Long threadId;
    private Long threadReplyId;

    private int reportCount;
    private int deleteCount;

    private LocalDateTime createdAt;
    private String quoteContent;

    private ReportStatus status;
    private String adminComment;

    public ReportPendingResponse() {}

    public ReportPendingResponse(
            Long reportId,
            Long reportedUserId,
            String targetNickname,
            Long threadId,
            Long threadReplyId,
            int reportCount,
            int deleteCount,
            LocalDateTime createdAt,
            String quoteContent,
            ReportStatus status,
            String adminComment
    ) {
        this.reportId = reportId;
        this.reportedUserId = reportedUserId;
        this.targetNickname = targetNickname;
        this.threadId = threadId;
        this.threadReplyId = threadReplyId;
        this.reportCount = reportCount;
        this.deleteCount = deleteCount;
        this.createdAt = createdAt;
        this.quoteContent = quoteContent;
        this.status = status;
        this.adminComment = adminComment;
    }
}

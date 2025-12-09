package com.cherry.cherrybookerbe.report.query.dto;

import com.cherry.cherrybookerbe.report.domain.ReportStatus;
import lombok.Getter;

import java.time.LocalDateTime;

// 신고 처리 목록 조회 dto
@Getter
public class ReportPendingResponse {
    private Long reportedUserId; // 유저 id fk
    private String targetNickname;

    private Long threadId; // 스레드 id fk
    private Long threadReplyId; // 스레드 답글 id fk

    private int reportCount; // 신고 횟수
    private int deleteCount; // 삭제 당한 횟수

    private LocalDateTime createdAt;
    private String quoteContent;

    private ReportStatus status; // pending, valid, rejected
    private String adminComment; // 관리자 코멘트

    // 기본 생성자
    public ReportPendingResponse() {
    }
    // 전체 생성자
    public ReportPendingResponse(
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

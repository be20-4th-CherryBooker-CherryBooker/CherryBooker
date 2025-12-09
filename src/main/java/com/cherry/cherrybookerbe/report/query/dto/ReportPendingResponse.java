package com.cherry.cherrybookerbe.report.query.dto;

import lombok.Getter;

import java.time.LocalDateTime;

// 신고 처리 목록 조회 dto
@Getter
public class ReportPendingResponse {
    private Long reportedUserId; // 유저 id fk
    private Long threadId; // 스레드 id fk
    private Long threadReplyId; // 스레드 답글 id fk
    private LocalDateTime createdAt;
    private int reportCount; // 신고 횟수
    private String targetNickname;
    private String content;

    // 기본 생성자
    public ReportPendingResponse() {
    }
    // 전체 생성자
    public ReportPendingResponse(
            Long reportedUserId,
            Long threadId,
            Long threadReplyId,
            LocalDateTime createdAt,
            int reportCount,
            String targetNickname,
            String content
    ) {
        this.reportedUserId = reportedUserId;
        this.threadId = threadId;
        this.threadReplyId = threadReplyId;
        this.createdAt = createdAt;
        this.reportCount = reportCount;
        this.targetNickname = targetNickname;
        this.content = content;
    }

}

package com.cherry.cherrybookerbe.report.command.dto;

import lombok.Getter;

// 신고 등록 ; 신고 1건 생성
@Getter
public class CreateReportRequest {
    private Long reporterId; // 신고자
    private Long threadId; // 스레드 게시글 신고 시 사용
    private Long threadsReplyId; // 스레드 답글 신고 시 사용

    public CreateReportRequest() {}
}

package com.cherry.cherrybookerbe.report.domain;

import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityReply;
import com.cherry.cherrybookerbe.community.command.domain.entity.CommunityThread;
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    //신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 신고 대상 스레드 (게시글 신고)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "threads_id") // nullable = true
    private CommunityThread threads;

    // 신고 대상 댓글 fk 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "threads_reply_id")
    private CommunityReply threadsReply;

    //신고 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    //관리자 처리 코멘트
    @Column(length = 500)
    private String adminComment;

    @Builder
    public Report(User user, CommunityThread threads, CommunityReply threadsReply, ReportStatus status, String adminComment) {
        this.user = user;
        this.threads = threads;
        this.threadsReply = threadsReply;
        this.status = status;
        this.adminComment = adminComment;
    }

    // 신고 승인 처리
    public void approve(String adminComment) {
        this.status = ReportStatus.VALID;
        this.adminComment = adminComment;
    }
    // 신고 반려 처리
    public void reject(String adminComment) {
        this.status = ReportStatus.REJECTED;
        this.adminComment = adminComment;
    }

}

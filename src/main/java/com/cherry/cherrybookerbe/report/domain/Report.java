package com.cherry.cherrybookerbe.report.domain;

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

    // 신고 대상 스레드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "threads_id", nullable = false)
    private CommunityThread threads;

    //신고 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    @Builder
    public Report(User user, CommunityThread threads, ReportStatus status) {
        this.user = user;
        this.threads = threads;
        this.status = status;

    }

    // 신고 승인 처리
    public void approve() {
        this.status = ReportStatus.VALID;
    }
    // 신고 반려 처리
    public void reject() {
        this.status = ReportStatus.REJECTED;
    }

}

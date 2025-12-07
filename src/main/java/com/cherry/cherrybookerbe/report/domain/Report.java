package com.cherry.cherrybookerbe.report.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Report {
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
    private Threads threads; // JPA 연관관계 규칙에 따라 단수형 엔티티로 매핑하는 것을 권장.

    //신고 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    //신고 시간
    //@CreationTimestamp ; jpa 상에서 자동생성할 경우.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 게시물 삭제 여부
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
    // private boolean isDeleted = false;

}

package com.cherry.cherrybookerbe.community.command.domain.entity;

import com.cherry.cherrybookerbe.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "threads_reply")
public class CommunityReply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "threads_reply_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "threads_id", nullable = false)
    private CommunityThread thread;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Integer userId;

    // 답변으로 단 글귀 ID
    @Column(name = "quote_id", nullable = false)
    private Integer quoteId;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // 한 번이라도 수정되었는지 여부 ("수정됨" 표시용)
    @Column(name = "modified", nullable = false)
    private boolean modified = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "report_count", nullable = false)
    private int reportCount = 0;

    @Builder
    private CommunityReply(CommunityThread thread,
                           Integer userId,
                           Integer quoteId) {
        this.thread = thread;
        this.userId = userId;
        this.quoteId = quoteId;
    }

    public void markModified() {
        this.modified = true;
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void increaseReportCount() {
        this.reportCount++;
    }
}

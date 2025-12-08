package com.cherry.cherrybookerbe.community.domain.entity;

import com.cherry.cherrybookerbe.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "threads")
public class CommunityThread extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "threads_id")
    private Integer id;

    @Column(name = "threads_reply_id")
    private Integer threadsReplyId;

    // 유저 PK
    @Column(name = "user_id", nullable = false, updatable = false)
    private Integer userId;

    // 글귀 PK
    @Column(name = "quote_id", nullable = false)
    private Integer quoteId;

    // 삭제 여부
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // 신고 누적 횟수
    @Column(name = "report_count", nullable = false)
    private int reportCount = 0;

    // 스레드에 달린 답변들
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL)
    private List<CommunityReply> replies = new ArrayList<>();

    @Builder
    private CommunityThread(Integer threadsReplyId,
                            Integer userId,
                            Integer quoteId) {
        this.threadsReplyId = threadsReplyId;
        this.userId = userId;
        this.quoteId = quoteId;
    }


    public void markDeleted() {
        this.deleted = true;
    }

    public void increaseReportCount() {
        this.reportCount++;
    }

    public void addReply(CommunityReply reply) {
        this.replies.add(reply);
    }
}

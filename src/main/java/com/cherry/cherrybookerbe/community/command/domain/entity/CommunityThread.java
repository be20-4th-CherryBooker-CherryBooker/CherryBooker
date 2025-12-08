package com.cherry.cherrybookerbe.community.command.domain.entity;

import com.cherry.cherrybookerbe.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    // 부모 스레드 (최초 스레드는 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommunityThread parent;

    // 자식 스레드들(릴레이)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<CommunityThread> children = new ArrayList<>();

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

    @Builder
    private CommunityThread(CommunityThread parent,
                            Integer userId,
                            Integer quoteId) {
        this.parent = parent;   // null 이면 최초 스레드
        this.userId = userId;
        this.quoteId = quoteId;
    }

    // 루트 스레드인지 여부 (최초 스레드)
    public boolean isRoot() {
        return this.parent == null;
    }

    // 내용(quote) 수정
    public void updateThread(Integer newQuoteId) {
        this.quoteId = newQuoteId;
    }

    // 자기 자신만 소프트 삭제
    public void markDeletedOnly() {
        this.deleted = true;
    }

    // 자기 + 모든 자식(릴레이)까지 소프트 삭제
    public void markDeletedCascade() {
        markDeletedOnly();
        for (CommunityThread child : children) {
            child.markDeletedCascade();
        }
    }

    public void increaseReportCount() {
        this.reportCount++;
    }

    // 양방향 연관관계 편의 메서드
    public void addChild(CommunityThread child) {
        this.children.add(child);
        child.parent = this;
    }

    // "수정됨" 여부
    public boolean isUpdated() {
        return updatedAt != null && !updatedAt.equals(createdAt);
    }
}

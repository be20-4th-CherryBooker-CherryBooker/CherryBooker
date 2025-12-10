package com.cherry.cherrybookerbe.community.query.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CommunityThreadDetailResponse {

    private final Integer threadId;
    private final Integer userId;
    private final Integer quoteId;
    private final String quoteContent;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final boolean updated;
    private final boolean deleted;
    private final int reportCount;
    private final List<CommunityReplyResponse> replies;

    public CommunityThreadDetailResponse(Integer threadId,
                                         Integer userId,
                                         Integer quoteId,
                                         String quoteContent,
                                         LocalDateTime createdAt,
                                         LocalDateTime updatedAt,
                                         boolean updated,
                                         boolean deleted,
                                         int reportCount,
                                         List<CommunityReplyResponse> replies) {
        this.threadId = threadId;
        this.userId = userId;
        this.quoteId = quoteId;
        this.quoteContent = quoteContent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updated = updated;
        this.deleted = deleted;
        this.reportCount = reportCount;
        this.replies = replies;
    }

}

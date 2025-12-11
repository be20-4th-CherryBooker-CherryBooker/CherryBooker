package com.cherry.cherrybookerbe.community.query.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommunityThreadSummaryResponse {

    private final Integer threadId;
    private final Integer userId;
    private final String userNickname;
    private final Integer quoteId;
    private final String quoteContent;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final boolean updated;
    private final boolean deleted;
    private final int reportCount;

    public CommunityThreadSummaryResponse(Integer threadId,
                                          Integer userId,
                                          String userNickname,
                                          Integer quoteId,
                                          String quoteContent,
                                          LocalDateTime createdAt,
                                          LocalDateTime updatedAt,
                                          boolean updated,
                                          boolean deleted,
                                          int reportCount) {
        this.threadId = threadId;
        this.userId = userId;
        this.userNickname = userNickname;
        this.quoteId = quoteId;
        this.quoteContent = quoteContent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updated = updated;
        this.deleted = deleted;
        this.reportCount = reportCount;
    }

}

package com.cherry.cherrybookerbe.community.query.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommunityReplyResponse {

    private final Integer replyId;
    private final Integer userId;
    private final Integer quoteId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final boolean updated;
    private final boolean deleted;
    private final int reportCount;

    public CommunityReplyResponse(Integer replyId,
                                  Integer userId,
                                  Integer quoteId,
                                  LocalDateTime createdAt,
                                  LocalDateTime updatedAt,
                                  boolean updated,
                                  boolean deleted,
                                  int reportCount) {
        this.replyId = replyId;
        this.userId = userId;
        this.quoteId = quoteId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updated = updated;
        this.deleted = deleted;
        this.reportCount = reportCount;
    }

}

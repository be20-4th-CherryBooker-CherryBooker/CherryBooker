package com.cherry.cherrybookerbe.community.query.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommunityThreadSummaryResponse {

    private final Integer threadId;
    private final Integer userId;
//    private final String nickname;
    private final Integer quoteId;
//    private final String bookTitle;
//    private final String quoteText;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final boolean updated;
    private final boolean deleted;
    private final int reportCount;

    public CommunityThreadSummaryResponse(Integer threadId,
                                          Integer userId,
//                                          String nickname,
                                          Integer quoteId,
//                                          String bookTitle,
//                                          String quoteText,
                                          LocalDateTime createdAt,
                                          LocalDateTime updatedAt,
                                          boolean updated,
                                          boolean deleted,
                                          int reportCount) {
        this.threadId = threadId;
        this.userId = userId;
//        this.nickname = nickname;
        this.quoteId = quoteId;
//        this.bookTitle = bookTitle;
//        this.quoteText = quoteText;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updated = updated;
        this.deleted = deleted;
        this.reportCount = reportCount;
    }

}

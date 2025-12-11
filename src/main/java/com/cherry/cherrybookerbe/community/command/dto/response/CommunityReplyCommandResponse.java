package com.cherry.cherrybookerbe.community.command.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommunityReplyCommandResponse {

    private final Integer replyId;
    private final boolean updated;
    private final LocalDateTime updatedAt;

    public CommunityReplyCommandResponse(Integer replyId, boolean updated, LocalDateTime updatedAt) {
        this.replyId = replyId;
        this.updated = updated;
        this.updatedAt = updatedAt;
    }

}
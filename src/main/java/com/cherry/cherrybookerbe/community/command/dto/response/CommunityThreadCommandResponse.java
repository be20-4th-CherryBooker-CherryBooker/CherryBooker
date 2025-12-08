package com.cherry.cherrybookerbe.community.command.dto.response;

import lombok.Getter;

@Getter
public class CommunityThreadCommandResponse {

    private final Integer threadId;
    private final boolean updated;

    public CommunityThreadCommandResponse(Integer threadId, boolean updated) {
        this.threadId = threadId;
        this.updated = updated;
    }

}
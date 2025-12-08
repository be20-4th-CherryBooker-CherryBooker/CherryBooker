package com.cherry.cherrybookerbe.community.command.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateCommunityReplyRequest {

    @NotNull
    private Integer userId;

    @NotNull
    private Integer quoteId;

}

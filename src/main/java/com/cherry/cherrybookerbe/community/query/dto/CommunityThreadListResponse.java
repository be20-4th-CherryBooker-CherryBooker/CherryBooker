package com.cherry.cherrybookerbe.community.query.dto;// package com.cherry.cherrybookerbe.community.query.dto;

import com.cherry.cherrybookerbe.common.dto.Pagination;
import lombok.Getter;

import java.util.List;

@Getter
public class CommunityThreadListResponse {

    private final List<CommunityThreadSummaryResponse> threads;
    private final Pagination pagination;

    public CommunityThreadListResponse(List<CommunityThreadSummaryResponse> threads,
                                       Pagination pagination) {
        this.threads = threads;
        this.pagination = pagination;
    }
}

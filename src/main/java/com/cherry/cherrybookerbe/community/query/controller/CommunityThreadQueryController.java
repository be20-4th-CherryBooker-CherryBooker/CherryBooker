package com.cherry.cherrybookerbe.community.query.controller;

import com.cherry.cherrybookerbe.community.query.dto.CommunityThreadDetailResponse;
import com.cherry.cherrybookerbe.community.query.dto.CommunityThreadSummaryResponse;
import com.cherry.cherrybookerbe.community.query.service.CommunityThreadQueryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community/threads")
public class CommunityThreadQueryController {

    private final CommunityThreadQueryService communityThreadQueryService;

    public CommunityThreadQueryController(CommunityThreadQueryService communityThreadQueryService) {
        this.communityThreadQueryService = communityThreadQueryService;
    }

    @GetMapping
    public ResponseEntity<List<CommunityThreadSummaryResponse>> getThreadList() {
        return ResponseEntity.ok(communityThreadQueryService.getThreadList());
    }

    @GetMapping("/{threadId}")
    public ResponseEntity<CommunityThreadDetailResponse> getThreadDetail(@PathVariable Integer threadId) {
        return ResponseEntity.ok(communityThreadQueryService.getThreadDetail(threadId));
    }
}

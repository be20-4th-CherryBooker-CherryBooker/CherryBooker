package com.cherry.cherrybookerbe.quote.query.controller;

import com.cherry.cherrybookerbe.quote.query.dto.QuoteDetailResponse;
import com.cherry.cherrybookerbe.quote.query.dto.QuoteListResponse;
import com.cherry.cherrybookerbe.quote.query.service.QuoteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quotes")
public class QuoteQueryController {

    private final QuoteQueryService quoteQueryService;

    // 글귀 상세 조회
    @GetMapping("/{quoteId}")
    public ResponseEntity<QuoteDetailResponse> getQuoteDetail(@PathVariable Long quoteId) {
        QuoteDetailResponse response = quoteQueryService.getQuoteDetail(quoteId);
        return ResponseEntity.ok(response);
    }

    // 내 글귀 전체 조회
    @GetMapping("/my/all")
    public ResponseEntity<List<QuoteListResponse>> getAllMyQuotes(
            @AuthenticationPrincipal OAuth2User user
    ) {
        Long userId = Long.valueOf(user.getAttribute("userId"));
        return ResponseEntity.ok(quoteQueryService.getQuotesByUser(userId));
    }

    // 내 글귀 페이징 조회 (무한스크롤)
    @GetMapping("/my")
    public ResponseEntity<Page<QuoteListResponse>> getMyQuotesPaged(
            @AuthenticationPrincipal OAuth2User user,
            @PageableDefault(size = 12) Pageable pageable
    ) {
        Long userId = Long.valueOf(user.getAttribute("userId"));
        return ResponseEntity.ok(quoteQueryService.getQuotesByUserPaged(userId, pageable));
    }
}

package com.cherry.cherrybookerbe.quote.query.controller;

import com.cherry.cherrybookerbe.common.security.auth.UserPrincipal;
import com.cherry.cherrybookerbe.quote.query.dto.QuoteDetailResponse;
import com.cherry.cherrybookerbe.quote.query.dto.QuoteListResponse;
import com.cherry.cherrybookerbe.quote.query.service.QuoteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
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
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal.userId().longValue();
        return ResponseEntity.ok(quoteQueryService.getQuotesByUser(userId));
    }

    // 내 글귀 페이징 조회 (검색 + 무한스크롤)
    @GetMapping("/my")
    public ResponseEntity<Page<QuoteListResponse>> getMyQuotesPaged(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 12) Pageable pageable,
            @RequestParam(required = false) String keyword
    ) {
        Long userId = principal.userId().longValue();
        return ResponseEntity.ok(
                quoteQueryService.getQuotesByUserPaged(userId, keyword, pageable)
        );
    }

    // 책 제목으로 검색
    @GetMapping
    public Page<QuoteListResponse> searchQuotes(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return quoteQueryService.searchQuotes(keyword, pageable);
    }
}

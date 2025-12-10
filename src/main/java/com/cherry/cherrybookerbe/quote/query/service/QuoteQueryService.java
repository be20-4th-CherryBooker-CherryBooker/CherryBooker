package com.cherry.cherrybookerbe.quote.query.service;

import com.cherry.cherrybookerbe.common.enums.Status;
import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import com.cherry.cherrybookerbe.quote.query.dto.QuoteDetailResponse;
import com.cherry.cherrybookerbe.quote.query.dto.QuoteListResponse;
import com.cherry.cherrybookerbe.quote.query.repository.QuoteQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteQueryService {

    private final QuoteQueryRepository repository;

    // 글귀 상세 조회
    public QuoteDetailResponse getQuoteDetail(Long quoteId) {
        Quote quote = repository.findById(quoteId)
                .filter(q -> q.getStatus() == Status.Y)
                .orElseThrow(() -> new RuntimeException("Quote not found or deleted"));

        return QuoteDetailResponse.from(quote);
    }

    // 내 글귀 전체 조회
    public List<QuoteListResponse> getQuotesByUser(Long userId) {
        return repository.findByUserIdAndStatus(userId, Status.Y).stream()
                .map(QuoteListResponse::from)
                .collect(Collectors.toList());
    }

    // 내 글귀 페이징 조회 (검색 + 무한스크롤)
    public Page<QuoteListResponse> getQuotesByUserPaged(Long userId, String keyword, Pageable pageable) {

        Page<Quote> result;

        // 검색어가 없으면 전체 조회
        if (keyword == null || keyword.isBlank()) {
            result = repository.findByUserIdAndStatus(userId, Status.Y, pageable);
        }
        // 검색어가 있으면 제목 검색
        else {
            result = repository.findByUserIdAndStatusAndBookTitleContaining(
                    userId, Status.Y, keyword, pageable);
        }

        return result.map(QuoteListResponse::from);
    }

    // 책 제목으로 검색
    public Page<QuoteListResponse> searchQuotes(String keyword, Pageable pageable) {
        Page<Quote> result = repository.searchByBookTitle(keyword, Status.Y, pageable);
        return result.map(QuoteListResponse::from);
    }

}

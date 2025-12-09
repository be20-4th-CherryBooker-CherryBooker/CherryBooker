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
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        return QuoteDetailResponse.from(quote);
    }

    // 내 글귀 전체 조회
    public List<QuoteListResponse> getQuotesByUser(Long userId) {
        return repository.findByUserIdAndStatus(userId, Status.Y).stream()
                .map(QuoteListResponse::from)
                .collect(Collectors.toList());
    }

    // 내 글귀 페이징 조회 (무한스크롤)
    public Page<QuoteListResponse> getQuotesByUserPaged(Long userId, Pageable pageable) {
        return repository.findByUserIdAndStatus(userId, Status.Y, pageable)
                .map(QuoteListResponse::from);
    }
}

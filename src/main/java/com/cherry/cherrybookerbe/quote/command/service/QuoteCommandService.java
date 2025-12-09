package com.cherry.cherrybookerbe.quote.command.service;

import com.cherry.cherrybookerbe.quote.command.dto.CreateQuoteRequest;
import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import com.cherry.cherrybookerbe.quote.command.repository.QuoteCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuoteCommandService {

    private final QuoteCommandRepository repository;

    // 구절 등록
    public Long createQuote(CreateQuoteRequest request) {

        Quote quote = Quote.builder()
                .userId(request.getUserId())
                .userBookId(request.getUserBookId())
                .content(request.getContent())
                .bookTitle(request.getBookTitle())
                .author(request.getAuthor())
                .imagePath(request.getImagePath())
                .comment(request.getComment())
                .build();

        repository.save(quote);
        return quote.getQuoteId();
    }

    // 구절 삭제 (soft delete)
    public void deleteQuote(Long quoteId) {
        Quote quote = repository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        quote.delete();  // 도메인 메서드
    }

    // 코멘트 수정
    public void updateComment(Long quoteId, String comment) {
        Quote quote = repository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        quote.updateComment(comment); // 도메인 메서드
    }

    // 코멘트 삭제
    public void deleteComment(Long quoteId) {
        Quote quote = repository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        quote.removeComment(); // 도메인 메서드
    }
}

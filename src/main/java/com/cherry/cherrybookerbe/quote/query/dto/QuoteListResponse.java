package com.cherry.cherrybookerbe.quote.query.dto;

import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteListResponse {

    private final Long quoteId;
    private final String content;
    private final String bookTitle;
    private final String author;
    private final String comment;
    private final String imagePath;
    private final String createdAt;

    public static QuoteListResponse from(Quote quote) {
        return QuoteListResponse.builder()
                .quoteId(quote.getQuoteId())
                .content(quote.getContent())
                .bookTitle(quote.getBookTitle())
                .author(quote.getAuthor())
                .comment(quote.getComment())
                .imagePath(quote.getImagePath())
                .createdAt(quote.getCreatedAt().toString())
                .build();
    }
}

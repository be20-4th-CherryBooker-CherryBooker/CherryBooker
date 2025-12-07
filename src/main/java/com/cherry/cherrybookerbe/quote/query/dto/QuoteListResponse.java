package com.cherry.cherrybookerbe.quote.query.dto;

import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuoteListResponse {

    private Long quoteId;
    private String content;
    private String bookTitle;
    private String author;
    private String comment;
    private String imagePath;
    private String createdAt;

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

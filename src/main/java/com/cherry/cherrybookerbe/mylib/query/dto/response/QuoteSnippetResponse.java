package com.cherry.cherrybookerbe.mylib.query.dto.response;

import com.cherry.cherrybookerbe.quote.command.entity.Quote;

import java.time.LocalDateTime;

public record QuoteSnippetResponse(
        Long quoteId,
        String content,
        String comment,
        String imagePath,
        LocalDateTime createdAt
) {

    public static QuoteSnippetResponse from(Quote quote) {
        return new QuoteSnippetResponse(
                quote.getQuoteId(),
                quote.getContent(),
                quote.getComment(),
                quote.getImagePath(),
                quote.getCreatedAt()
        );
    }
}

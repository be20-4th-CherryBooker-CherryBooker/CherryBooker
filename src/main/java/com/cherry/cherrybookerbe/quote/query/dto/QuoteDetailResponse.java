package com.cherry.cherrybookerbe.quote.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuoteDetailResponse {
    private Long quoteId;
    private String content;
    private String bookTitle;
    private String author;
    private String comment;
    private String imagePath;
    private String createdAt;
}

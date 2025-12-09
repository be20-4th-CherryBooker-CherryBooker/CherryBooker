package com.cherry.cherrybookerbe.quote.command.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CreateQuoteRequest {

    private Long userId;
    private Long userBookId;
    private String content;
    private String bookTitle;
    private String author;
    private String imagePath;
    private String comment;

    @Builder
    public CreateQuoteRequest(Long userId,
                              Long userBookId,
                              String content,
                              String bookTitle,
                              String author,
                              String imagePath,
                              String comment) {
        this.userId = userId;
        this.userBookId = userBookId;
        this.content = content;
        this.bookTitle = bookTitle;
        this.author = author;
        this.imagePath = imagePath;
        this.comment = comment;
    }
}

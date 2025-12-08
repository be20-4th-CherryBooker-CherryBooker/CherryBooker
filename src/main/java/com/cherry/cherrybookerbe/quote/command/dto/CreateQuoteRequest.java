package com.cherry.cherrybookerbe.quote.command.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateQuoteRequest {

    private Long userId;
    private Long userBookId;
    private String content;
    private String bookTitle;
    private String author;
    private String imagePath;
    private String comment;
}

package com.cherry.cherrybookerbe.mylib.query.dto.response;

import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;

import java.util.List;

public record MyBookDetailResponse(
        Long myLibId,
        Long bookId,
        BookStatus status,
        String title,
        String author,
        String coverImageUrl,
        List<QuoteSnippetResponse> quotes,
        boolean badgeIssued
) {
}

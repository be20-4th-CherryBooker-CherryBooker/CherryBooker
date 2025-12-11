package com.cherry.cherrybookerbe.mylib.command.application.dto.response;

public record BookMetadataResponse(
        String title,
        String author,
        String isbn,
        String coverImageUrl
) {
}

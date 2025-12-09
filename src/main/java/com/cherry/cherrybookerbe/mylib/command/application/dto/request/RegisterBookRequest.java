package com.cherry.cherrybookerbe.mylib.command.application.dto.request;


public record RegisterBookRequest(
        Long userId,
        String keyword,
        String isbnHint
) {
}

package com.cherry.cherrybookerbe.mylib.command.application.dto;


public record RegisterBookRequest(
        Long userId,
        String keyword,
        String isbnHint
) {
}

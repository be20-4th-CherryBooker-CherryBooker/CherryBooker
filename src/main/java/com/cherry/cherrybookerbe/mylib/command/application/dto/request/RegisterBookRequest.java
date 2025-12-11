package com.cherry.cherrybookerbe.mylib.command.application.dto.request;


public record RegisterBookRequest(
        String keyword,
        String isbnHint
) {
}

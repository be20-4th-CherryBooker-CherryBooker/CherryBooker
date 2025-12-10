package com.cherry.cherrybookerbe.mylib.query.service;

import com.cherry.cherrybookerbe.mylib.command.application.dto.response.BookMetadataResponse;
import com.cherry.cherrybookerbe.mylib.command.infrastructure.service.LibraryOpenApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LibraryBookSearchService {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 30;

    private final LibraryOpenApiClient libraryOpenApiClient;

    public List<BookMetadataResponse> searchByTitle(String keyword, Integer requestedSize) {
        int size = normalizeSize(requestedSize);
        return libraryOpenApiClient.searchBooksByTitle(keyword, size);
    }

    private int normalizeSize(Integer requestedSize) {
        if (requestedSize == null) {
            return DEFAULT_SIZE;
        }
        return Math.min(Math.max(requestedSize, 1), MAX_SIZE);
    }
}

package com.cherry.cherrybookerbe.mylib.command.domain.service;

import com.cherry.cherrybookerbe.common.exception.BadRequestException;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.Book;
import com.cherry.cherrybookerbe.mylib.command.domain.repository.SearchBookRepository;
import com.cherry.cherrybookerbe.mylib.command.application.dto.response.BookMetadataResponse;
import com.cherry.cherrybookerbe.mylib.command.infrastructure.service.LibraryOpenApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterNewBookService {

    private final SearchBookRepository searchBookRepository;
    private final LibraryOpenApiClient bookApiClient;

    public Book findOrCreate(String keyword, String isbnHint) {
        if (!StringUtils.hasText(keyword)) {
            throw new BadRequestException("도서 제목이 필요합니다.");
        }

        if (StringUtils.hasText(isbnHint)) {
            return searchBookRepository.findByIsbn(isbnHint.trim())
                    .orElseGet(() -> createBookFromApi(keyword, isbnHint));
        }

        return searchBookRepository.findFirstByTitleIgnoreCase(keyword.trim())
                .orElseGet(() -> createBookFromApi(keyword, isbnHint));
    }

    private Book createBookFromApi(String keyword, String isbnHint) {
        BookMetadataResponse metadata = bookApiClient.search(keyword, isbnHint);
        Book book = Book.builder()
                .title(trim(metadata.title(), 20))
                .author(trim(metadata.author(), 20))
                .isbn(trim(metadata.isbn(), 20))
                .coverImageUrl(trim(metadata.coverImageUrl(), 500))
                .build();
        return searchBookRepository.save(book);
    }

    private String trim(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }
}

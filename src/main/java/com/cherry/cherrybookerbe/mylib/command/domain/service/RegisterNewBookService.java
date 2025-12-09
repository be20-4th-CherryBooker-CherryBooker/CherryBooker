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

// 도메인 로직을 처리하므로 Domain layer의 서비스로 추가
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterNewBookService {

    private final SearchBookRepository searchBookRepository;
    private final LibraryOpenApiClient bookApiClient;

    // 키워드를 받아서
    // 주의: 일단 책 제목만으로는 부족해서 ISBN을 받는 것으로 구현
    public Book findOrCreate(String keyword, String isbnHint) {
        if (!StringUtils.hasText(keyword)) {
            throw new BadRequestException("도서 제목이 필요합니다.");
        }

        if (StringUtils.hasText(isbnHint)) {
            // 1. CherryBooker 자체 데이터베이스에서 찾거나
            return searchBookRepository.findByIsbn(isbnHint.trim())
                    // 2. 없으면 경기도사이버 도서관에서 검색하여 자체 데이터베이스에 저장 후 출력
                    .orElseGet(() -> createBookFromApi(keyword, isbnHint));
        }

        return searchBookRepository.findFirstByTitleIgnoreCase(keyword.trim())
                .orElseGet(() -> createBookFromApi(keyword, isbnHint));
    }

    // 경기도 사이버 도서관에 도서 검색 후 CherryBooker 자체 데이터베이스에 저장
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

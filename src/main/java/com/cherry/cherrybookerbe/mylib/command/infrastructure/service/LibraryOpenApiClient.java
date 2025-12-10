package com.cherry.cherrybookerbe.mylib.command.infrastructure.service;

import com.cherry.cherrybookerbe.common.exception.BadRequestException;
import com.cherry.cherrybookerbe.common.exception.ExternalApiException;
import com.cherry.cherrybookerbe.mylib.command.application.dto.response.BookMetadataResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class LibraryOpenApiClient {

    private static final int MAX_PAGE_SIZE = 30;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private final String defaultCoverImage;

    public LibraryOpenApiClient(RestTemplateBuilder restTemplateBuilder,
                                ObjectMapper objectMapper,
                                @Value("${external.book-api.url:https://ebook.library.kr/api/open-search/ebook}") String baseUrl,
                                @Value("${external.book-api.ServiceKey:nOf3PUHtiQyc5sOQlImwkRbvzxWHvXXgWp6L76HS0ZEqfqi1N2wK2DuoonoE2O68}") String apiKey,
                                @Value("${external.book-api.default-cover-image:https://ebook.library.kr/images/no-cover.png}") String defaultCoverImage) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.defaultCoverImage = defaultCoverImage;
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    public BookMetadataResponse search(String keyword, String isbnHint) {
        if (!StringUtils.hasText(keyword)) {
            throw new BadRequestException("도서 제목이 필요합니다.");
        }

        JsonNode docNode = searchDocs(searchType(isbnHint), searchKeyword(keyword, isbnHint), 1)
                .stream()
                .findFirst()
                .orElse(null);
        if (docNode == null && StringUtils.hasText(isbnHint)) {
            docNode = searchDocs("title", keyword.trim(), 1).stream().findFirst().orElse(null);
        }
        if (docNode == null) {
            throw new ExternalApiException("검색 조건에 맞는 도서를 찾지 못했습니다.");
        }

        return toMetadata(docNode, keyword, isbnHint);
    }

    public List<BookMetadataResponse> searchBooksByTitle(String keyword, int size) {
        if (!StringUtils.hasText(keyword)) {
            throw new BadRequestException("도서 제목이 필요합니다.");
        }
        int normalizedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        List<JsonNode> docs = searchDocs("title", keyword.trim(), normalizedSize);
        return docs.stream()
                .map(doc -> toMetadata(doc, keyword, null))
                .toList();
    }

    private List<JsonNode> searchDocs(String searchType, String searchKeyword, int size) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("ServiceKey", apiKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", Math.max(1, size))
                .queryParam("ebookType", "ebook")
                .queryParam("searchType", searchType)
                .queryParam("searchKeyword", searchKeyword)
                .build()
                .encode()
                .toUri();

        String payload;
        try {
            payload = restTemplate.getForObject(uri, String.class);
        } catch (Exception e) {
            throw new ExternalApiException("국립도서관 API 호출에 실패했습니다.", e);
        }

        if (!StringUtils.hasText(payload)) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            List<String> candidates = Arrays.asList("/data", "/result/docs", "/docs", "/channel/item", "/items", "/item");
            List<JsonNode> documents = new ArrayList<>();
            for (String path : candidates) {
                JsonNode node = root.at(path);
                if (node.isArray() && node.size() > 0) {
                    node.forEach(documents::add);
                    return documents;
                }
                if (node.isObject() && node.size() > 0) {
                    documents.add(node);
                    return documents;
                }
            }
        } catch (Exception e) {
            throw new ExternalApiException("국립도서관 API 응답을 파싱하지 못했습니다.", e);
        }
        return List.of();
    }

    private BookMetadataResponse toMetadata(JsonNode docNode, String keyword, String isbnHint) {
        String title = firstText(docNode, keyword, "titleInfo", "title", "TITLE");
        String author = firstText(docNode, "미상", "authorInfo", "author", "AUTHOR");
        String isbn = firstText(docNode, StringUtils.hasText(isbnHint) ? isbnHint : keyword, "isbn", "isbn13", "isbn10", "ISBN");
        String coverImageUrl = firstText(docNode, defaultCoverImage, "imageUrl", "cover", "bookImageURL", "COVER_URL");
        return new BookMetadataResponse(title, author, isbn, coverImageUrl);
    }

    private String firstText(JsonNode node, String defaultValue, String... fieldNames) {
        if (node != null) {
            for (String name : fieldNames) {
                JsonNode valueNode = node.get(name);
                if (valueNode != null && valueNode.isTextual() && StringUtils.hasText(valueNode.asText())) {
                    return valueNode.asText();
                }
            }
        }
        return defaultValue;
    }

    private String searchType(String isbnHint) {
        return StringUtils.hasText(isbnHint) ? "isbn" : "title";
    }

    private String searchKeyword(String keyword, String isbnHint) {
        return StringUtils.hasText(isbnHint) ? isbnHint.trim() : keyword.trim();
    }
}

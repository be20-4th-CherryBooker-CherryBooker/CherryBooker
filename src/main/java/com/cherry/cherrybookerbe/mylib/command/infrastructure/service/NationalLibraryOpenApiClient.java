package com.cherry.cherrybookerbe.mylib.command.infrastructure.service;

import com.cherry.cherrybookerbe.common.exception.BadRequestException;
import com.cherry.cherrybookerbe.common.exception.ExternalApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Service
public class NationalLibraryOpenApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private final String defaultCoverImage;

    public NationalLibraryOpenApiClient(RestTemplateBuilder restTemplateBuilder,
                                        ObjectMapper objectMapper,
                                        @Value("${external.book-api.url:https://www.nl.go.kr/NL/search/openApi/search.do}") String baseUrl,
                                        @Value("${external.book-api.key:8661828ebf544e1b971df87a9bfae2be365ae16451d530bfb1f666d30021fa27}") String apiKey,
                                        @Value("${external.book-api.default-cover:https://dummyimage.com/600x800/cfcfcf/333333&text=CherryBooker}") String defaultCoverImage) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.defaultCoverImage = defaultCoverImage;
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }

    public BookMetadata search(String keyword, String isbnHint) {
        if (!StringUtils.hasText(keyword)) {
            throw new BadRequestException("도서 제목이 필요합니다.");
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("key", apiKey)
                .queryParam("apiType", "json")
                .queryParam("pageSize", 1)
                .queryParam("kwd", keyword.trim())
                .build(true)
                .toUri();

        String payload;
        try {
            payload = restTemplate.getForObject(uri, String.class);
        } catch (Exception e) {
            throw new ExternalApiException("국립도서관 API 호출에 실패했습니다.", e);
        }

        JsonNode docNode = extractDocNode(payload);
        String title = firstText(docNode, keyword, "titleInfo", "title");
        String author = firstText(docNode, "미상", "authorInfo", "author");
        String isbn = firstText(docNode, StringUtils.hasText(isbnHint) ? isbnHint : keyword, "isbn", "isbn13", "isbn10");
        String coverImageUrl = firstText(docNode, defaultCoverImage, "imageUrl", "cover", "bookImageURL");

        return new BookMetadata(title, author, isbn, coverImageUrl);
    }

    private JsonNode extractDocNode(String payload) {
        if (!StringUtils.hasText(payload)) {
            throw new ExternalApiException("국립도서관 API 응답이 비어 있습니다.");
        }
        try {
            JsonNode root = objectMapper.readTree(payload);
            List<String> candidates = Arrays.asList("/result/docs", "/docs", "/channel/item", "/items", "/item");
            for (String path : candidates) {
                JsonNode node = root.at(path);
                if (node.isArray() && node.size() > 0) {
                    return node.get(0);
                }
                if (node.isObject() && node.size() > 0) {
                    return node;
                }
            }
        } catch (Exception e) {
            throw new ExternalApiException("국립도서관 API 응답을 파싱하지 못했습니다.", e);
        }
        throw new ExternalApiException("검색 조건에 맞는 도서를 찾지 못했습니다.");
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

    public record BookMetadata(
            String title,
            String author,
            String isbn,
            String coverImageUrl
    ) {
    }
}

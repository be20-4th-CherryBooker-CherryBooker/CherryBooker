package com.cherry.cherrybookerbe.ocr.client;

import com.cherry.cherrybookerbe.ocr.dto.OcrResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

// OcrClient는 별도 패키지(client)에 두어서 외부 서버 호출을 전담
@Component
@RequiredArgsConstructor
public class OcrClient {

    private final RestTemplate restTemplate;

    public String requestOcr(String imagePath) {

        File file = new File(imagePath);
        if (!file.exists()) {
            throw new RuntimeException("이미지 파일이 존재하지 않습니다: " + imagePath);
        }

        // === HTTP 헤더 ===
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // === Multipart Body ===
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, Object>> entity =
                new HttpEntity<>(body, headers);

        String url = "http://localhost:8000/ocr";

        ResponseEntity<OcrResponse> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, OcrResponse.class);

        return response.getBody().getFullText();
    }
}

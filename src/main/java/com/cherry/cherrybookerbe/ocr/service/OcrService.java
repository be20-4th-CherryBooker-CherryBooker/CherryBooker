package com.cherry.cherrybookerbe.ocr.service;

import com.cherry.cherrybookerbe.ocr.client.OcrClient;
import com.cherry.cherrybookerbe.ocr.dto.OcrResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.io.File;

@Service
@RequiredArgsConstructor
public class OcrService {

    private final OcrClient ocrClient;
    private final RestTemplate restTemplate;

    public String extractText(String imagePath) {

        File file = new File(imagePath);

        if (!file.exists()) {
            throw new RuntimeException("이미지 파일을 찾을 수 없습니다: " + imagePath);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<OcrResponse> response = restTemplate.exchange(
                    "http://127.0.0.1:8000/ocr",
                    HttpMethod.POST,
                    requestEntity,
                    OcrResponse.class
            );

            return response.getBody().getFullText();

        } catch (Exception e) {
            throw new RuntimeException("OCR 서버 호출 실패", e);
        }
    }

}

package com.cherry.cherrybookerbe.ocr.client;

import com.cherry.cherrybookerbe.ocr.dto.OcrResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

// OcrClient는 별도 패키지(client)에 두어서 외부 서버 호출을 전담
@Component
public class OcrClient {

    public String requestOcr(String imagePath) {

        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> body = new HashMap<>();
        body.put("imagePath", imagePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        String url = "http://localhost:8000/ocr";

        OcrResponse response = restTemplate.postForObject(url, request, OcrResponse.class);

        return response.getText();
    }
}

package com.cherry.cherrybookerbe.ocr.service;

import com.cherry.cherrybookerbe.ocr.client.OcrClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OcrService {

    private final OcrClient ocrClient;

    public String extractText(String imagePath) {
        return ocrClient.requestOcr(imagePath);
    }
}

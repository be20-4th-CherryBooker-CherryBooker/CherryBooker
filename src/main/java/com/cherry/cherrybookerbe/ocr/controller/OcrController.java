package com.cherry.cherrybookerbe.ocr.controller;

import com.cherry.cherrybookerbe.ocr.dto.OcrRequest;
import com.cherry.cherrybookerbe.ocr.dto.OcrResponse;
import com.cherry.cherrybookerbe.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 이미지 경로 받아서 OCR 서비스 호출하고 OCR 결과를 프론트에 응답
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ocr")
public class OcrController {

    private final OcrService ocrService;

    @PostMapping("/extract")
    public OcrResponse extract(@RequestBody OcrRequest request) {
        return new OcrResponse(ocrService.extractText(request.getImagePath()));
    }
}

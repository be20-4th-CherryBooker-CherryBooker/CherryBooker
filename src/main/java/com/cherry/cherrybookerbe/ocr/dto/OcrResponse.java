package com.cherry.cherrybookerbe.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 추출된 텍스트
@Getter
@AllArgsConstructor
public class OcrResponse {
    private String text;
}


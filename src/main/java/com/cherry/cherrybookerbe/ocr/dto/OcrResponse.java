package com.cherry.cherrybookerbe.ocr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 추출된 텍스트
@Getter
@NoArgsConstructor
public class OcrResponse {

    @JsonProperty("full_text")
    private String fullText;

    public OcrResponse(String fullText) {
        this.fullText = fullText;
    }
}


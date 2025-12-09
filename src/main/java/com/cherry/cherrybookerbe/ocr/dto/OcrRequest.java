package com.cherry.cherrybookerbe.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 이미지 주소
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OcrRequest {
    private String imagePath;
}

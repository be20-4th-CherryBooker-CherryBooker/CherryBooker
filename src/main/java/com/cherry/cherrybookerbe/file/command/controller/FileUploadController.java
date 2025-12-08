package com.cherry.cherrybookerbe.file.command.controller;

import com.cherry.cherrybookerbe.file.command.dto.UploadResponse;
import com.cherry.cherrybookerbe.file.command.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    public UploadResponse upload(@RequestParam MultipartFile file) {

        // 이미지 저장 수행
        String savedPath = fileUploadService.save(file);

        return new UploadResponse(savedPath);
    }
}

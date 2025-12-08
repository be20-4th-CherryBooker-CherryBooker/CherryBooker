package com.cherry.cherrybookerbe.file.command.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final String uploadBasePath = "/uploads/quotes/";

    public String save(MultipartFile file) {

        // 확장자 체크
        validateExtension(file.getOriginalFilename());

        // 파일명 생성
        String uuid = UUID.randomUUID().toString();
        String extension = getExtension(file.getOriginalFilename());
        String fileName = uuid + "." + extension;

        // 저장 경로 생성
        Path savePath = Paths.get(uploadBasePath, fileName);

        try {
            Files.createDirectories(savePath.getParent());
            file.transferTo(savePath.toFile());
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        return savePath.toString(); // DB에 저장할 이미지 경로
    }

    private void validateExtension(String filename) {
        String ext = getExtension(filename).toLowerCase();
        if (!(ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg"))) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}

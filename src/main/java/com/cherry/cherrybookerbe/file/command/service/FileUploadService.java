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

    // 상대경로가 아니라 절대경로 기반으로 생성하기 위해 basePath만 유지
    private final String uploadBasePath = "uploads/quotes/";

    public String save(MultipartFile file) {

        // 확장자 체크
        validateExtension(file.getOriginalFilename());

        // 파일명 생성
        String uuid = UUID.randomUUID().toString();
        String extension = getExtension(file.getOriginalFilename());
        String fileName = uuid + "." + extension;

        // 실제 저장될 절대경로 생성 (프로젝트 루트 기반)
        Path uploadDir = Paths.get(System.getProperty("user.dir"), uploadBasePath);

        try {
            // uploads/quotes 폴더 없다면 생성
            Files.createDirectories(uploadDir);

            // 저장할 파일 경로
            Path savePath = uploadDir.resolve(fileName);

            // 파일 저장
            file.transferTo(savePath.toFile());

            // DB에는 상대 경로만 저장
            return uploadBasePath + fileName;

        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
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

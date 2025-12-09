package com.cherry.cherrybookerbe.quote;

import com.cherry.cherrybookerbe.ocr.dto.OcrRequest;
import com.cherry.cherrybookerbe.quote.command.dto.CreateQuoteRequest;
import com.cherry.cherrybookerbe.quote.command.dto.UpdateCommentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/quote_dummy.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class QuoteCommandIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================== QTE-001 이미지 업로드 ========================
    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    @DisplayName("QTE-001 이미지 업로드 성공")
    void uploadImage_success() throws Exception {

        // MockMultipartFile 생성
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    // ========================= QTE-002 OCR 텍스트 추출 =========================
    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    @DisplayName("QTE-002 OCR 텍스트 추출 - 프로젝트 내부 이미지 사용")
    void extractOcrText_realApiCall_success() throws Exception {

        // 프로젝트 루트 경로 가져오기
        String projectPath = System.getProperty("user.dir");

        // 내부 uploads/quotes/test-ocr.jpg 절대경로로 변환
        String imagePath = projectPath + "/uploads/quotes/test-ocr.jpg";

        OcrRequest request = new OcrRequest(imagePath);

        mockMvc.perform(post("/api/ocr/extract")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.full_text").isNotEmpty());
    }

    // =========================== QTE-003 글귀 등록 ===========================
    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    @DisplayName("QTE-003 글귀 등록")
    void createQuote_success() throws Exception {

        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,          // userId
                100L,        // userBookId
                "감정이 없는 건 슬픔을 모르는 게 아니었다.",
                "아몬드",
                "손원평",
                null,
                null
        );

        mockMvc.perform(post("/api/quotes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quoteId").exists());
    }

    // =========================== QTE-004 글귀 삭제 ===========================
    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    @DisplayName("QTE-004 글귀 삭제")
    void deleteQuote_success() throws Exception {

        mockMvc.perform(delete("/api/quotes/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    // ====================== QTE-005 글귀 코멘트 등록 ========================
    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    @DisplayName("QTE-005 글귀 코멘트 등록")
    void addComment_success() throws Exception {

        UpdateCommentRequest request = new UpdateCommentRequest("좋은 문장이네");

        mockMvc.perform(patch("/api/quotes/1/comment")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // ====================== QTE-006 글귀 코멘트 수정 ========================
    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    @DisplayName("QTE-006 글귀 코멘트 수정")
    void updateComment_success() throws Exception {

        UpdateCommentRequest request = new UpdateCommentRequest("수정된 코멘트입니다");

        mockMvc.perform(patch("/api/quotes/1/comment")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // ====================== QTE-007 글귀 코멘트 삭제 ========================
    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    @DisplayName("QTE-007 글귀 코멘트 삭제")
    void deleteComment_success() throws Exception {

        mockMvc.perform(delete("/api/quotes/1/comment")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}

package com.cherry.cherrybookerbe.mylib.command.application.controller;

import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.MyLib;
import com.cherry.cherrybookerbe.mylib.command.domain.repository.MyLibRepository;
import com.cherry.cherrybookerbe.mylib.command.infrastructure.service.LibraryOpenApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/dummy_mylib.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BookStateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MyLibRepository myLibRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LibraryOpenApiClient libraryOpenApiClient;

    private static final LibraryOpenApiClient.BookMetadata SAMPLE_BOOK =
            new LibraryOpenApiClient.BookMetadata(
                    "초보자를 위한 Java 200제",
                    "조효은",
                    "9788956747859",
                    "https://www.library.kr/attachfile/DRMContent/ebook/4808956747859/L4808956747859.jpg"
            );

    @Test
    @WithMockUser(username = "tester@example.com")
    @DisplayName("LIB-001~006: 사용자는 도서를 등록하고 글귀 등록 트리거를 거쳐 읽는 중/읽은 책 상태로 전환할 수 있다.")
    void register_and_progress_book_lifecycle() throws Exception {
        given(libraryOpenApiClient.search("초보자를 위한 Java 200제", "9788956747859"))
                .willReturn(SAMPLE_BOOK);

        MvcResult registerResult = mockMvc.perform(post("/mylib/register-books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "keyword": "초보자를 위한 Java 200제",
                                  "isbnHint": "9788956747859"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("WISH"))
                .andExpect(jsonPath("$.data.newlyRegistered").value(true))
                .andReturn();

        JsonNode registeredPayload = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        long myLibId = registeredPayload.get("data").get("myLibId").asLong();

        mockMvc.perform(patch("/mylib/books/{myLibId}/status", myLibId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetStatus": "READING",
                                  "trigger": "QUOTE_CAPTURE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.previousStatus").value("WISH"))
                .andExpect(jsonPath("$.data.currentStatus").value("READING"))
                .andExpect(jsonPath("$.data.badgeIssued").value(false));

        mockMvc.perform(patch("/mylib/books/{myLibId}/status", myLibId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetStatus": "READ"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.changed").value(true))
                .andExpect(jsonPath("$.data.previousStatus").value("READING"))
                .andExpect(jsonPath("$.data.currentStatus").value("READ"))
                .andExpect(jsonPath("$.data.badgeIssued").value(true));

        MyLib progressed = myLibRepository.findWithBookByMyLibId(myLibId).orElseThrow();
        assertThat(progressed.getBookStatus()).isEqualTo(BookStatus.READ);
        assertThat(progressed.getBook().getTitle()).isEqualTo(SAMPLE_BOOK.title());
        assertThat(progressed.getBook().getCoverImageUrl()).isEqualTo(SAMPLE_BOOK.coverImageUrl());
    }
}

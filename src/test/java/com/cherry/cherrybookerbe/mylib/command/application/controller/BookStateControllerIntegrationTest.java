package com.cherry.cherrybookerbe.mylib.command.application.controller;

import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.MyLib;
import com.cherry.cherrybookerbe.mylib.command.domain.repository.MyLibRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @WithMockUser(username = "tester@example.com")
    @DisplayName("MYLIB-CMD-001: 나의 서재에 책을 등록하면 기본 상태는 WISH다")
    void registerBook_addsNewWishEntry() throws Exception {
        mockMvc.perform(post("/mylib/register-books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "keyword": "클린 코드",
                                  "isbnHint": "ISBN-003"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WISH"))
                .andExpect(jsonPath("$.newlyRegistered").value(true));

        Optional<MyLib> myLib = myLibRepository.findByUserUserIdAndBookBookId(1, 3L);
        assertThat(myLib).isPresent();
        assertThat(myLib.get().getBookStatus()).isEqualTo(BookStatus.WISH);
    }

    @Test
    @WithMockUser(username = "tester@example.com")
    @DisplayName("MYLIB-CMD-002: 글귀 등록 트리거로 WISH 상태를 READING으로 전환한다")
    void changeBookStatus_movesWishToReading() throws Exception {
        mockMvc.perform(patch("/mylib/books/{myLibId}/status", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetStatus": "READING",
                                  "trigger": "QUOTE_CAPTURE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changed").value(true))
                .andExpect(jsonPath("$.currentStatus").value("READING"))
                .andExpect(jsonPath("$.badgeIssued").value(false));

        MyLib myLib = myLibRepository.findById(10L).orElseThrow();
        assertThat(myLib.getBookStatus()).isEqualTo(BookStatus.READING);
    }
}

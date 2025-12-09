package com.cherry.cherrybookerbe.mylib.command.application.controller;

import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.MyLib;
import com.cherry.cherrybookerbe.mylib.command.domain.repository.MyLibRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
    @DisplayName("LIB-001: 사용자는 책 제목을 검색하여 나의 서재에 책을 등록할 수 있다. 나의 서재에 등록된 도서는 처음에는 \"읽고 싶은 책\" 상태로 등록된다.")
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
    @DisplayName("LIB-006: 글귀 등록 트리거로 WISH 상태를 READING으로 전환한다, 사용자가 \"읽고 싶은 책(Wish)\"을 클릭하고 등록하고자 하는 글귀를 촬영, 글귀와 관련된 코멘트를 작성 완료하면, 작성 완료 즉시 \"읽고 싶은 책\"이 \"읽는 중\"인 책으로 변경된다. ")
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

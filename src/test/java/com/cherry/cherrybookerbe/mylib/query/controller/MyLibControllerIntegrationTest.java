package com.cherry.cherrybookerbe.mylib.query.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/dummy_mylib.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MyLibControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "tester@example.com")
    @DisplayName("LIB-002: 나의 서재 목록을 조회하면 상태별 카드가 내려온다")
    void getMyLibrary_returnsPagedBooks() throws Exception {
        mockMvc.perform(get("/mylib/books")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.dummySlots").value(6))
                .andExpect(jsonPath("$.books[0].title").value("이펙티브 자바"))
                .andExpect(jsonPath("$.books[0].displayType").value("SPINE"))
                .andExpect(jsonPath("$.books[0].badgeIssued").value(true))
                .andExpect(jsonPath("$.books[1].title").value("자바의 정석"))
                .andExpect(jsonPath("$.books[1].displayType").value("COVER"));
    }

    @Test
    @WithMockUser(username = "tester@example.com")
    @DisplayName("LIB-004: 특정 도서의 글귀 목록을 조회한다")
    void getBookQuotes_returnsQuoteList() throws Exception {
        mockMvc.perform(get("/mylib/books/{myLibId}/quotes", 11L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(2))
                .andExpect(jsonPath("$.badgeIssued").value(true))
                .andExpect(jsonPath("$.quotes[0].content").value("멋진 문장"))
                .andExpect(jsonPath("$.quotes[0].comment").value("코멘트"));
    }
}

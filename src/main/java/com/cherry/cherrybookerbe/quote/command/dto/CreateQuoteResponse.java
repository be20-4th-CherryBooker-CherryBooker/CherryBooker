package com.cherry.cherrybookerbe.quote.command.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

//응답을 DTO로 감싸서 JSON 객체로 반환하도록 변경하기
@Getter
@AllArgsConstructor
public class CreateQuoteResponse {
    private Long quoteId;
}

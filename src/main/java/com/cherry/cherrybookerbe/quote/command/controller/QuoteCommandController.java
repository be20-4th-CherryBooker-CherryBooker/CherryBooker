package com.cherry.cherrybookerbe.quote.command.controller;

import com.cherry.cherrybookerbe.quote.command.dto.CreateQuoteRequest;
import com.cherry.cherrybookerbe.quote.command.service.QuoteCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cherry.cherrybookerbe.quote.command.dto.UpdateCommentRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quotes")
public class QuoteCommandController {

    private final QuoteCommandService quoteCommandService;

    // 글귀 등록
    @PostMapping
    public ResponseEntity<Long> createQuote(@RequestBody CreateQuoteRequest request) {
        Long quoteId = quoteCommandService.createQuote(request);
        return ResponseEntity.ok(quoteId);
    }

    // 글귀 삭제
    @DeleteMapping("/{quoteId}")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long quoteId) {
        quoteCommandService.deleteQuote(quoteId);
        return ResponseEntity.noContent().build();
    }

    // 코멘트 수정
    @PatchMapping("/{quoteId}/comment")
    public ResponseEntity<Void> updateComment(@PathVariable Long quoteId,
                                              @RequestBody UpdateCommentRequest request) {
        quoteCommandService.updateComment(quoteId, request.getComment());
        return ResponseEntity.ok().build();
    }

    // 코멘트 삭제
    @DeleteMapping("/{quoteId}/comment")
    public ResponseEntity<Void> deleteComment(@PathVariable Long quoteId) {
        quoteCommandService.deleteComment(quoteId);
        return ResponseEntity.noContent().build();
    }
}

package com.cherry.cherrybookerbe.mylib.command.application.controller;

import com.cherry.cherrybookerbe.common.dto.ApiResponse;
import com.cherry.cherrybookerbe.common.exception.BadRequestException;
import com.cherry.cherrybookerbe.common.security.auth.UserPrincipal;
import com.cherry.cherrybookerbe.mylib.command.application.dto.request.BookStatusChangeRequest;
import com.cherry.cherrybookerbe.mylib.command.application.dto.request.RegisterBookRequest;
import com.cherry.cherrybookerbe.mylib.command.application.dto.response.BookStatusChangeResponse;
import com.cherry.cherrybookerbe.mylib.command.application.dto.response.RegisterBookResponse;
import com.cherry.cherrybookerbe.mylib.command.application.service.BookStatusService;
import com.cherry.cherrybookerbe.mylib.command.application.service.RegisterBookService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mylib")
public class BookStateController {

    private final RegisterBookService registerBookService;
    private final BookStatusService bookStatusService;

    @Operation(operationId = "", summary = " ", description = " ")
    @PostMapping("/register-books")
    public ResponseEntity<ApiResponse<RegisterBookResponse>> registerBook(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RegisterBookRequest request) {
        if (userPrincipal == null) {
            throw new BadRequestException("로그인이 필요합니다.");
        }

        RegisterBookResponse response = registerBookService.register(userPrincipal.userId(), request);
        HttpStatus status = response.newlyRegistered() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiResponse.success(response));
    }

    @Operation(operationId = "", summary = " ", description = " ")
    @PatchMapping("/books/{myLibId}/status")
    public ResponseEntity<ApiResponse<BookStatusChangeResponse>> changeBookStatus(
            @PathVariable Long myLibId,
            @Valid @RequestBody BookStatusChangeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookStatusService.changeStatus(myLibId, request)));
    }
}

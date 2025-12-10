package com.cherry.cherrybookerbe.mylib.query.controller;

import com.cherry.cherrybookerbe.common.dto.ApiResponse;
import com.cherry.cherrybookerbe.common.security.auth.UserPrincipal;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.query.dto.request.MyLibrarySearchRequest;
import com.cherry.cherrybookerbe.mylib.query.dto.response.MyBookDetailResponse;
import com.cherry.cherrybookerbe.mylib.query.dto.response.MyLibrarySliceResponse;
import com.cherry.cherrybookerbe.mylib.query.service.MyLibSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mylib")
public class MyLibController {

    private final MyLibSearchService myLibSearchService;

    @GetMapping("/books")
    public ResponseEntity<ApiResponse<MyLibrarySliceResponse>> getMyLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BookStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        MyLibrarySearchRequest request = new MyLibrarySearchRequest(userPrincipal.userId(), keyword, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(myLibSearchService.getMyLibrary(request)));
    }

    @GetMapping("/books/{myLibId}/quotes")
    public ResponseEntity<ApiResponse<MyBookDetailResponse>> getBookQuotes(@PathVariable Long myLibId) {
        return ResponseEntity.ok(ApiResponse.success(myLibSearchService.getBookDetail(myLibId)));
    }
}

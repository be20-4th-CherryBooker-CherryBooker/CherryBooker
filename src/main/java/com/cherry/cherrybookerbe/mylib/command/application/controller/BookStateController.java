package com.cherry.cherrybookerbe.mylib.command.application.controller;

import com.cherry.cherrybookerbe.mylib.command.application.dto.BookStatusChangeRequest;
import com.cherry.cherrybookerbe.mylib.command.application.dto.BookStatusChangeResponse;
import com.cherry.cherrybookerbe.mylib.command.application.dto.RegisterBookRequest;
import com.cherry.cherrybookerbe.mylib.command.application.dto.RegisterBookResponse;
import com.cherry.cherrybookerbe.mylib.command.application.service.BookStatusService;
import com.cherry.cherrybookerbe.mylib.command.application.service.RegisterBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mylib")
public class BookStateController {

    private final RegisterBookService registerBookService;
    private final BookStatusService bookStatusService;

    @PostMapping("/register-books")
    public ResponseEntity<RegisterBookResponse> registerBook(@RequestBody RegisterBookRequest request) {
        RegisterBookResponse response = registerBookService.register(request);
        HttpStatus status = response.newlyRegistered() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PatchMapping("/books/{myLibId}/status")
    public ResponseEntity<BookStatusChangeResponse> changeBookStatus(@PathVariable Long myLibId,
                                                                     @RequestBody BookStatusChangeRequest request) {
        return ResponseEntity.ok(bookStatusService.changeStatus(myLibId, request));
    }
}

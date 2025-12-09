package com.cherry.cherrybookerbe.mylib.command.application.dto.response;

import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.MyLib;

import java.time.LocalDateTime;

public record RegisterBookResponse(
        Long myLibId,
        Long bookId,
        BookStatus status,
        boolean newlyRegistered,
        LocalDateTime registeredAt
) {

    public static RegisterBookResponse of(MyLib myLib, boolean created) {
        return new RegisterBookResponse(
                myLib.getMyLibId(),
                myLib.getBook().getBookId(),
                myLib.getBookStatus(),
                created,
                myLib.getCreatedAt()
        );
    }
}

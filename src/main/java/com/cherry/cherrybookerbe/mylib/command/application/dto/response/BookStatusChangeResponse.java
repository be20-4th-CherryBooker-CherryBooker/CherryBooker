package com.cherry.cherrybookerbe.mylib.command.application.dto.response;

import com.cherry.cherrybookerbe.mylib.command.application.dto.StatusTrigger;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.MyLib;

public record BookStatusChangeResponse(
        Long myLibId,
        BookStatus previousStatus,
        BookStatus currentStatus,
        boolean changed,
        StatusTrigger trigger,
        boolean badgeIssued
) {

    public static BookStatusChangeResponse of(MyLib myLib,
                                              BookStatus previousStatus,
                                              boolean changed,
                                              StatusTrigger trigger) {
        BookStatus current = myLib.getBookStatus();
        return new BookStatusChangeResponse(
                myLib.getMyLibId(),
                previousStatus,
                current,
                changed,
                trigger,
                current == BookStatus.READ
        );
    }
}

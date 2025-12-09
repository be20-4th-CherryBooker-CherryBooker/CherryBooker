package com.cherry.cherrybookerbe.mylib.query.dto.response;

import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.MyLib;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MyBookCardResponse(
        Long myLibId,
        Long bookId,
        String title,
        String author,
        BookStatus status,
        String coverImageUrl,
        DisplayType displayType,
        boolean badgeIssued,
        LocalDateTime registeredAt
) {

    public static MyBookCardResponse from(MyLib myLib) {
        BookStatus status = myLib.getBookStatus();
        DisplayType displayType = status == BookStatus.READ ? DisplayType.SPINE : DisplayType.COVER;
        return MyBookCardResponse.builder()
                .myLibId(myLib.getMyLibId())
                .bookId(myLib.getBook().getBookId())
                .title(myLib.getBook().getTitle())
                .author(myLib.getBook().getAuthor())
                .status(status)
                .coverImageUrl(myLib.getBook().getCoverImageUrl())
                .displayType(displayType)
                .badgeIssued(status == BookStatus.READ)
                .registeredAt(myLib.getCreatedAt())
                .build();
    }

    public enum DisplayType {
        COVER,
        SPINE
    }
}

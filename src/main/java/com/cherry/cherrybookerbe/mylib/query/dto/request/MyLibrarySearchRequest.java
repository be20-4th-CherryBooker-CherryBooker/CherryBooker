package com.cherry.cherrybookerbe.mylib.query.dto.request;

import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import lombok.Getter;

@Getter
public class MyLibrarySearchRequest {

    private static final int DEFAULT_SIZE = 8;

    private final Integer userId;
    private final String keyword;
    private final BookStatus status;
    private final int page;
    private final int size;

    public MyLibrarySearchRequest(Integer userId,
                                  String keyword,
                                  BookStatus status,
                                  int page,
                                  int size) {
        this.userId = userId;
        this.keyword = keyword;
        this.status = status;
        this.page = Math.max(page, 0);
        this.size = size <= 0 ? DEFAULT_SIZE : size;
    }
}

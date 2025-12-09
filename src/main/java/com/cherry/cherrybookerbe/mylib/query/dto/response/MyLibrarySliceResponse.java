package com.cherry.cherrybookerbe.mylib.query.dto.response;

import java.util.List;

public record MyLibrarySliceResponse(
        long totalCount,
        int page,
        int size,
        boolean hasMore,
        List<MyBookCardResponse> books,
        int dummySlots,
        String notice
) {
}

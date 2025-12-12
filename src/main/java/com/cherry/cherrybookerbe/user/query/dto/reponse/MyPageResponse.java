package com.cherry.cherrybookerbe.user.query.dto.reponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageResponse {
    private String nickname;
    private TreeInfo treeInfo;
    private Statistics statistics;
    private BookStatusRatio bookStatusRatio;

    @Getter
    @Builder
    public static class TreeInfo {
        private int readBooksThisMonth;  // 이번 달 읽은 책 수
        private TreeStage treeStage;     // 나무 단계
    }

    @Getter
    @Builder
    public static class Statistics {
        private long totalReadBooks;     // 총 누적 완독 권수
        private long totalThreads;       // 총 스레드 수
        private long totalQuotes;        // 저장한 글귀 수
    }

    @Getter
    @Builder
    public static class BookStatusRatio {
        private long reading;
        private long read;
        private long wish;
        private double readingPercent;
        private double readPercent;
        private double wishPercent;
    }

    public enum TreeStage {
        STAGE1,      // 새싹 (0권)
        STAGE2,    // 작은 풀 (2권)
        STAGE3,     // 작은 나무 (6권)
        STAGE4    // 체리 나무 (10권)
    }
}
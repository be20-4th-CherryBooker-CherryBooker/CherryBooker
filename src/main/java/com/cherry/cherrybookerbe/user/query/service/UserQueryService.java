package com.cherry.cherrybookerbe.user.query.service;

import com.cherry.cherrybookerbe.community.command.domain.repository.CommunityThreadRepository;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.command.domain.repository.MyLibRepository;
import com.cherry.cherrybookerbe.quote.query.repository.QuoteQueryRepository;
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import com.cherry.cherrybookerbe.user.command.repository.UserRepository;
import com.cherry.cherrybookerbe.user.query.dto.reponse.MyPageResponse;
import com.cherry.cherrybookerbe.user.query.dto.reponse.MyProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final MyLibRepository myLibQueryRepository;
    private final CommunityThreadRepository threadQueryRepository;
    private final QuoteQueryRepository quoteQueryRepository;

    // 내 정보 조회
    public MyProfileResponse getMyProfile(Integer userId){
        User user =  userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다." + userId));
        return MyProfileResponse.from(user);
    }

    /* 마이페이지 조회 */
    public MyPageResponse getMyPageInfo(Integer userId) {
        log.info("마이페이지 통계 정보 조회 시작 - userId: {}", userId);

        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 이번 달 시작 날짜
        LocalDateTime startOfMonth = getStartOfCurrentMonth();

        // 3. 이번 달 READ 수
        long readBooksThisMonth = myLibQueryRepository
                .countByUserIdAndStatusThisMonth(userId, BookStatus.READ, startOfMonth);

        // 4. 나무 단계
        MyPageResponse.TreeStage treeStage = determineTreeStage(readBooksThisMonth);

        // 5. 통계 데이터
        long totalReadBooks = myLibQueryRepository
                .countByUserIdAndStatus(userId, BookStatus.READ);
        long totalThreads = threadQueryRepository.countByUserId(userId);
        long totalQuotes = quoteQueryRepository.countByUserId(userId.longValue());

        // 6. 책 상태별 비율
        long readingCount = myLibQueryRepository
                .countByUserIdAndBookStatus(userId, BookStatus.READING);
        long readCount = myLibQueryRepository
                .countByUserIdAndBookStatus(userId, BookStatus.READ);
        long wishCount = myLibQueryRepository
                .countByUserIdAndBookStatus(userId, BookStatus.WISH);

        long totalBooks = readingCount + readCount + wishCount;

        MyPageResponse.BookStatusRatio bookStatusRatio = MyPageResponse.BookStatusRatio.builder()
                .reading(readingCount)
                .read(readCount)
                .wish(wishCount)
                .readingPercent(totalBooks > 0 ? (readingCount * 100.0 / totalBooks) : 0)
                .readPercent(totalBooks > 0 ? (readCount * 100.0 / totalBooks) : 0)
                .wishPercent(totalBooks > 0 ? (wishCount * 100.0 / totalBooks) : 0)
                .build();

        log.info("마이페이지 정보 조회 완료 - 이번달 READ: {}, 총 READ: {}, 스레드: {}, 글귀: {}",
                readBooksThisMonth, totalReadBooks, totalThreads, totalQuotes);

        // 7. 응답 생성
        return MyPageResponse.builder()
                .nickname(user.getUserNickname())
                .treeInfo(MyPageResponse.TreeInfo.builder()
                        .readBooksThisMonth((int) readBooksThisMonth)
                        .treeStage(treeStage)
                        .build())
                .statistics(MyPageResponse.Statistics.builder()
                        .totalReadBooks(totalReadBooks)
                        .totalThreads(totalThreads)
                        .totalQuotes(totalQuotes)
                        .build())
                .bookStatusRatio(bookStatusRatio)
                .build();
    }

    // 이번 달의 첫날 00:00:00 시각 반환 메서드
    private LocalDateTime getStartOfCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();
        return currentMonth.atDay(1).atStartOfDay();
    }

    private MyPageResponse.TreeStage determineTreeStage(long readBooks) {
        if (readBooks >= 10) {
            return MyPageResponse.TreeStage.STAGE4;
        } else if (readBooks >= 6) {
            return MyPageResponse.TreeStage.STAGE3;
        } else if (readBooks >= 2) {
            return MyPageResponse.TreeStage.STAGE2;
        } else if (readBooks >= 0) {
            return MyPageResponse.TreeStage.STAGE1;
        }
        return MyPageResponse.TreeStage.STAGE1;
    }

}


package com.cherry.cherrybookerbe.mylib.command.domain.repository;

import com.cherry.cherrybookerbe.mylib.command.domain.entity.BookStatus;
import com.cherry.cherrybookerbe.mylib.command.domain.entity.MyLib;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MyLibRepository extends JpaRepository<MyLib, Long> {

    Optional<MyLib> findByUserUserIdAndBookBookId(Integer userId, Long bookId);

    @EntityGraph(attributePaths = "book")
    Optional<MyLib> findWithBookByMyLibId(Long myLibId);

    /* 마이페이지 조회 시 사용 */
    // 이번 달 READ 상태 책 수
    @Query("SELECT COUNT(m) FROM MyLib m WHERE m.user.userId = :userId " +
            "AND m.bookStatus = :status " +
            "AND m.updatedAt >= :startOfMonth")
    long countByUserIdAndStatusThisMonth(
            @Param("userId") Integer userId,
            @Param("status") BookStatus status,
            @Param("startOfMonth") LocalDateTime startOfMonth
    );

    // 총 누적 READ 수
    @Query("SELECT COUNT(m) FROM MyLib m WHERE m.user.userId = :userId " +
            "AND m.bookStatus = :status")
    long countByUserIdAndStatus(
            @Param("userId") Integer userId,
            @Param("status") BookStatus status
    );

    // 상태별 책 수 조회
    @Query("SELECT COUNT(m) FROM MyLib m WHERE m.user.userId = :userId " +
            "AND m.bookStatus = :status")
    long countByUserIdAndBookStatus(
            @Param("userId") Integer userId,
            @Param("status") BookStatus status
    );
}

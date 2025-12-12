package com.cherry.cherrybookerbe.quote.query.repository;

import com.cherry.cherrybookerbe.common.enums.Status;
import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuoteQueryRepository extends JpaRepository<Quote, Long> {

    // 전체 조회 (삭제되지 않은 글귀만)
    List<Quote> findByUserIdAndStatus(Long userId, Status status);

    // 페이징 조회 (무한스크롤)
    Page<Quote> findByUserIdAndStatus(Long userId, Status status, Pageable pageable);

    List<Quote> findByUserBookIdAndStatusOrderByCreatedAtDesc(Long userBookId, Status status);

    Page<Quote> findByUserIdAndStatusAndBookTitleContaining(
            Long userId,
            Status status,
            String keyword,
            Pageable pageable
    );

    @Query("SELECT q FROM Quote q WHERE q.status = :status AND (:keyword IS NULL OR q.bookTitle LIKE %:keyword%)")
    Page<Quote> searchByBookTitle(@Param("keyword") String keyword,
                                  @Param("status") Status status,
                                  Pageable pageable);

    /* 마이페이지 조회 시 사용 */
    @Query("SELECT COUNT(q) FROM Quote q " +
            "WHERE q.userId = :userId AND q.status = com.cherry.cherrybookerbe.common.enums.Status.Y")
    long countByUserId(@Param("userId") Long userId);
}

package com.cherry.cherrybookerbe.quote.query.repository;

import com.cherry.cherrybookerbe.common.enums.Status;
import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteQueryRepository extends JpaRepository<Quote, Long> {

    // 전체 조회 (삭제되지 않은 글귀만)
    List<Quote> findByUserIdAndStatus(Long userId, Status status);

    // 페이징 조회 (무한스크롤)
    Page<Quote> findByUserIdAndStatus(Long userId, Status status, Pageable pageable);

    List<Quote> findByUserBookIdAndStatusOrderByCreatedAtDesc(Long userBookId, Status status);
}

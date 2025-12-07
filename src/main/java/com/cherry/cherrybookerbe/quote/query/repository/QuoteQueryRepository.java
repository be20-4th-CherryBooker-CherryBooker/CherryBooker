package com.cherry.cherrybookerbe.quote.query.repository;

import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteQueryRepository extends JpaRepository<Quote, Long> {

    // 전체 조회용
    List<Quote> findByUserIdAndIsDeletedFalse(Long userId);

    // 페이징 조회용 (무한스크롤)
    Page<Quote> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
}

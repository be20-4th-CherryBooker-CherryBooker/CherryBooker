package com.cherry.cherrybookerbe.quote.command.repository;

import com.cherry.cherrybookerbe.quote.command.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteCommandRepository extends JpaRepository<Quote, Long> {
}

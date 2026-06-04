package com.tradepositiontracker.repository;

import com.tradepositiontracker.enums.TradeStatus;
import com.tradepositiontracker.model.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    Optional<Trade> findByTradeReference(String tradeReference);

    Page<Trade> findByStatus(TradeStatus status, Pageable pageable);
}

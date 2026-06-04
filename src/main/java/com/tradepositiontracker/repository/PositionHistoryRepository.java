package com.tradepositiontracker.repository;

import com.tradepositiontracker.model.PositionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionHistoryRepository extends JpaRepository<PositionHistory, Long> {

    List<PositionHistory> findByPartyOrderByTimestampDesc(String party);

    List<PositionHistory> findByPartyAndCurrencyOrderByTimestampDesc(String party, String currency);

    List<PositionHistory> findByTradeReferenceOrderByTimestampDesc(String tradeReference);
}

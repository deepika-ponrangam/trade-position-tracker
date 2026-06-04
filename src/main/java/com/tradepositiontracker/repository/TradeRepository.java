package com.tradepositiontracker.repository;

import com.tradepositiontracker.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByTradingPartyAndInstrument(String tradingParty, String instrument);
}

package com.tradepositiontracker.repository;

import com.tradepositiontracker.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByTradingPartyAndInstrument(String tradingParty, String instrument);

    List<Position> findByTradingParty(String tradingParty);
}

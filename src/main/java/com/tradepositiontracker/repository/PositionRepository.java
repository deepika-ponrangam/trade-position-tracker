package com.tradepositiontracker.repository;

import com.tradepositiontracker.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByPartyAndCurrencyAndValueDate(String party, String currency, LocalDate valueDate);

    List<Position> findByParty(String party);

    List<Position> findByPartyAndCurrency(String party, String currency);

    List<Position> findByPartyAndValueDate(String party, LocalDate valueDate);

    List<Position> findByPartyAndValueDateBetween(String party, LocalDate from, LocalDate to);

    List<Position> findByPartyAndValueDateGreaterThan(String party, LocalDate date);
}

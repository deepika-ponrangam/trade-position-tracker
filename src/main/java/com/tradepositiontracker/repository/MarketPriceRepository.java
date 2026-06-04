package com.tradepositiontracker.repository;

import com.tradepositiontracker.model.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

    Optional<MarketPrice> findByInstrument(String instrument);
}

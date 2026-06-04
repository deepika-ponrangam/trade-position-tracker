package com.tradepositiontracker.service;

import com.tradepositiontracker.model.MarketPrice;
import com.tradepositiontracker.repository.MarketPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MarketPriceService {

    @Autowired
    private MarketPriceRepository marketPriceRepository;

    public MarketPrice updatePrice(String instrument, double price) {
        MarketPrice marketPrice = marketPriceRepository.findByInstrument(instrument)
                .orElse(new MarketPrice(instrument, price));
        marketPrice.setPrice(price);
        marketPrice.setUpdatedAt(LocalDateTime.now());
        return marketPriceRepository.save(marketPrice);
    }

    public MarketPrice getPrice(String instrument) {
        return marketPriceRepository.findByInstrument(instrument)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Market price not found for " + instrument));
    }
}

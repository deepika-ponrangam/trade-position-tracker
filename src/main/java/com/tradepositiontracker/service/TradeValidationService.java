package com.tradepositiontracker.service;

import com.tradepositiontracker.model.Trade;
import org.springframework.stereotype.Service;

@Service
public class TradeValidationService {

    public void validate(Trade trade) {
        if (trade.getTradingParty() == null || trade.getTradingParty().isBlank()) {
            throw new IllegalArgumentException("Trading party is required");
        }
        if (trade.getInstrument() == null || trade.getInstrument().isBlank()) {
            throw new IllegalArgumentException("Instrument is required");
        }
        if (trade.getSide() == null ||
                (!trade.getSide().equals("BUY") && !trade.getSide().equals("SELL"))) {
            throw new IllegalArgumentException("Side must be BUY or SELL");
        }
        if (trade.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (trade.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }
}

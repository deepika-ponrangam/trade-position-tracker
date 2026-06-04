package com.tradepositiontracker.service;

import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TradeValidationService {

    private final TradeRepository tradeRepository;

    public void validateNewTrade(Trade trade) {
        if (trade.getTradeReference() == null || trade.getTradeReference().isBlank()) {
            throw new IllegalArgumentException("Trade reference is required");
        }
        if (tradeRepository.findByTradeReference(trade.getTradeReference()).isPresent()) {
            throw new IllegalArgumentException("Trade reference already exists: " + trade.getTradeReference());
        }
        validateTradeFields(trade);
    }

    public void validateAmendment(Trade trade) {
        validateTradeFields(trade);
    }

    private void validateTradeFields(Trade trade) {
        if (trade.getTradingParty() == null || trade.getTradingParty().isBlank()) {
            throw new IllegalArgumentException("Trading party is required");
        }
        if (trade.getCounterParty() == null || trade.getCounterParty().isBlank()) {
            throw new IllegalArgumentException("Counter party is required");
        }
        if (trade.getTradingParty().equalsIgnoreCase(trade.getCounterParty())) {
            throw new IllegalArgumentException("Trading party and counter party must be different");
        }
        if (trade.getPrimaryCurrency() == null || trade.getPrimaryCurrency().isBlank()) {
            throw new IllegalArgumentException("Primary currency is required");
        }
        if (trade.getSecondaryCurrency() == null || trade.getSecondaryCurrency().isBlank()) {
            throw new IllegalArgumentException("Secondary currency is required");
        }
        if (trade.getPrimaryCurrency().equalsIgnoreCase(trade.getSecondaryCurrency())) {
            throw new IllegalArgumentException("Primary and secondary currencies must be different");
        }
        if (trade.getPrimaryAmount() == null || trade.getPrimaryAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Primary amount must be positive");
        }
        if (trade.getSecondaryAmount() == null || trade.getSecondaryAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Secondary amount must be positive");
        }
        if (trade.getDirection() == null) {
            throw new IllegalArgumentException("Direction is required (BUY or SELL)");
        }
        if (trade.getValueDate() == null) {
            throw new IllegalArgumentException("Value date is required");
        }
    }
}

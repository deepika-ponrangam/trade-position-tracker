package com.tradepositiontracker.service;

import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.repository.TradeRepository;
import com.tradepositiontracker.util.CurrencyValidator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;

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
        
        CurrencyValidator.validate(trade.getPrimaryCurrency());
        CurrencyValidator.validate(trade.getSecondaryCurrency());

        if (trade.getPrimaryCurrency().equalsIgnoreCase(trade.getSecondaryCurrency())) {
            throw new IllegalArgumentException("Primary and secondary currencies must be different");
        }
        if (trade.getPrimaryAmount() == null || trade.getPrimaryAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Primary amount must be positive");
        }
        if (trade.getSecondaryAmount() == null || trade.getSecondaryAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Secondary amount must be positive");
        }

        validateDecimalPrecision(trade.getPrimaryCurrency(), trade.getPrimaryAmount(),"Primary Amount");
        validateDecimalPrecision(trade.getSecondaryCurrency(), trade.getSecondaryAmount(),"Secondary Amount");

        if (trade.getDirection() == null) {
            throw new IllegalArgumentException("Direction is required (BUY or SELL)");
        }
        if (trade.getValueDate() == null) {
            throw new IllegalArgumentException("Value date is required");
        }
    }
    private void validateDecimalPrecision(String currencyCode, BigDecimal amount, String fieldName) {
        Currency currency = Currency.getInstance(currencyCode);
        int allowedDecimals = currency.getDefaultFractionDigits();
        if (allowedDecimals < 0) {
            throw new IllegalArgumentException("Unsupported currency: " + currencyCode);
        }
        int actualDecimals = amount.stripTrailingZeros().scale();
        if (actualDecimals > allowedDecimals) {
            throw new IllegalArgumentException(String.format(
                "%s exceeds allowed decimal precision. %s allows a maximum of %d decimal places, but got %d.",
                fieldName, currencyCode, allowedDecimals, actualDecimals));
        }
    }
}

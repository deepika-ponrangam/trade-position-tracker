package com.tradepositiontracker.service;

import com.tradepositiontracker.model.ExchangeRate;
import com.tradepositiontracker.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRate updateRate(String currency, BigDecimal rateToUsd) {
        ExchangeRate rate = exchangeRateRepository.findByCurrency(currency.toUpperCase())
                .orElseGet(() -> new ExchangeRate(currency.toUpperCase(), rateToUsd));
        rate.setRateToUsd(rateToUsd);
        rate.setUpdatedAt(LocalDateTime.now());
        return exchangeRateRepository.save(rate);
    }

    public ExchangeRate getRate(String currency) {
        if (currency == null){
            throw new IllegalArgumentException("Currency cannot be null");
        }
        return exchangeRateRepository.findByCurrency(currency.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Exchange rate not found for " + currency));
    }

    public BigDecimal getUsdEquivalent(String currency, BigDecimal amount) {
        if ("USD".equalsIgnoreCase(currency)) {
            return amount;
        }
        ExchangeRate rate = exchangeRateRepository.findByCurrency(currency.toUpperCase())
                .orElse(null);
        if (rate == null) {
            return null;
        }
        return amount.multiply(rate.getRateToUsd());
    }
}

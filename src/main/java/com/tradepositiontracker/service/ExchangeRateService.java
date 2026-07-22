package com.tradepositiontracker.service;

import com.tradepositiontracker.model.ExchangeRate;
import com.tradepositiontracker.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.tradepositiontracker.config.CacheConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateService self;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository,
                                @Lazy ExchangeRateService self) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.self = self;
    }
    @CachePut(cacheNames = CacheConfig.EXCHANGE_RATE_CACHE, key = "#currency.toUpperCase()")

    public ExchangeRate updateRate(String currency, BigDecimal rateToUsd) {
        ExchangeRate rate = exchangeRateRepository.findByCurrency(currency.toUpperCase())
                .orElseGet(() -> new ExchangeRate(currency.toUpperCase(), rateToUsd));
        rate.setRateToUsd(rateToUsd);
        rate.setUpdatedAt(LocalDateTime.now());
        return exchangeRateRepository.save(rate);
    }
    @Cacheable(cacheNames = CacheConfig.EXCHANGE_RATE_CACHE, key = "#currency.toUpperCase()")
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
        try{
            return amount.multiply(self.getRate(currency).getRateToUsd());
        }catch (IllegalArgumentException notFound){
            return null;
        }
    }
}

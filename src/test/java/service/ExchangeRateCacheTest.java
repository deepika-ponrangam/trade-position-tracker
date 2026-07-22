package com.tradepositiontracker.service;

import com.tradepositiontracker.config.CacheConfig;
import com.tradepositiontracker.model.ExchangeRate;
import com.tradepositiontracker.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ExchangeRateService.class, CacheConfig.class})
public class ExchangeRateCacheTest {
    @Autowired
    private ExchangeRateService exchangeRateService;
    @MockBean
    private ExchangeRateRepository exchangeRateRepository;
    @Test
    void repeatedLookupsForSameCurrencyDoNotHitDbAgain(){
        ExchangeRate eur = new ExchangeRate("EUR", new BigDecimal("1.1000"));
        ExchangeRate usd = new ExchangeRate("USD", new BigDecimal("1.00"));
        when(exchangeRateRepository.findByCurrency("EUR")).thenReturn(Optional.of(eur));
        when(exchangeRateRepository.findByCurrency("USD")).thenReturn(Optional.of(usd));
        exchangeRateService.getRate("EUR");
        exchangeRateService.getRate("USD");
        exchangeRateService.getRate("EUR");
        exchangeRateService.getRate("USD");
        verify(exchangeRateRepository, times(1)).findByCurrency("EUR");
        verify(exchangeRateRepository, times(1)).findByCurrency("USD");
    }
    
    @Test
    void updatingARateRefreshesTheCache(){
        ExchangeRate initial = new ExchangeRate("JPY", new BigDecimal("0.0067"));
        when(exchangeRateRepository.findByCurrency("JPY")).thenReturn(Optional.of(initial));
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ExchangeRate beforeUpdate = exchangeRateService.getRate("JPY");
        assertThat(beforeUpdate.getRateToUsd()).isEqualByComparingTo("0.0067");
        exchangeRateService.updateRate("JPY",new BigDecimal("0.0082"));
        ExchangeRate afterupdate = exchangeRateService.getRate("JPY");
        assertThat(afterupdate.getRateToUsd()).isEqualByComparingTo("0.0082");
        verify(exchangeRateRepository, times(2)).findByCurrency("JPY");
    }

    @Test
    void usingSameCache(){
        ExchangeRate gbp = new ExchangeRate("GBP", new BigDecimal("1.2500"));
        when(exchangeRateRepository.findByCurrency("GBP")).thenReturn(Optional.of(gbp));
        exchangeRateService.getRate("GBP");
        BigDecimal usd = exchangeRateService.getUsdEquivalent("GBP", new BigDecimal("100"));
        assertThat(usd).isEqualByComparingTo("125.00");
        verify(exchangeRateRepository, times(1)).findByCurrency("GBP");
    }

}

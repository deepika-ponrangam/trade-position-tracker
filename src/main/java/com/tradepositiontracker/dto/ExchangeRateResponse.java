package com.tradepositiontracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tradepositiontracker.model.ExchangeRate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateResponse {

    private Long id;
    private String currency;
    private BigDecimal rateToUsd;
    private LocalDateTime updatedAt;
    public static ExchangeRateResponse fromEntity(ExchangeRate exchangeRate) {
        if (exchangeRate == null) {
            return null;
        }
        
        return ExchangeRateResponse.builder()
                .id(exchangeRate.getId())
                .currency(exchangeRate.getCurrency())
                .rateToUsd(exchangeRate.getRateToUsd())
                .updatedAt(exchangeRate.getUpdatedAt())
                .build();
    }
}

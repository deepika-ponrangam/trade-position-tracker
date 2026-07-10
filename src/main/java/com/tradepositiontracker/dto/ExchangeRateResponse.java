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
    public static ExchangeRateResponse fromEntity(ExchangeRate rate) {
        if (rate == null) return null;
        return ExchangeRateResponse.builder()
                .id(rate.getId())
                .currency(rate.getCurrency())
                .rateToUsd(rate.getRateToUsd())
                .updatedAt(rate.getUpdatedAt())
                .build();
    }
}

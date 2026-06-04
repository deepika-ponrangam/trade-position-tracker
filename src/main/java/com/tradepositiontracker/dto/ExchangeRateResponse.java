package com.tradepositiontracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExchangeRateResponse {

    private Long id;
    private String currency;
    private BigDecimal rateToUsd;
    private LocalDateTime updatedAt;
}

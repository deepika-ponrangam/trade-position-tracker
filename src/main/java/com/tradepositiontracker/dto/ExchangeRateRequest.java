package com.tradepositiontracker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRateRequest {

    private String currency;
    private BigDecimal rateToUsd;
}

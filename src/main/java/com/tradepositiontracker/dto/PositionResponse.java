package com.tradepositiontracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PositionResponse {

    private Long id;
    private String party;
    private String currency;
    private LocalDate valueDate;
    private BigDecimal exposure;
    private BigDecimal obligation;
    private BigDecimal netPosition;
    private BigDecimal usdEquivalent;
}

package com.tradepositiontracker.dto;

import com.tradepositiontracker.enums.Direction;
import com.tradepositiontracker.enums.TradeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TradeResponse {

    private Long id;
    private String tradeReference;
    private String tradingParty;
    private String counterParty;
    private String primaryCurrency;
    private BigDecimal primaryAmount;
    private String secondaryCurrency;
    private BigDecimal secondaryAmount;
    private Direction direction;
    private LocalDate valueDate;
    private LocalDate tradeDate;
    private TradeStatus status;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

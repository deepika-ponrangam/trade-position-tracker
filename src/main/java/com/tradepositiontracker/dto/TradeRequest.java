package com.tradepositiontracker.dto;

import com.tradepositiontracker.enums.Direction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TradeRequest {

    private String tradeReference;
    private String tradingParty;
    private String counterParty;
    private String primaryCurrency;
    private BigDecimal primaryAmount;
    private String secondaryCurrency;
    private BigDecimal secondaryAmount;
    private Direction direction;
    private LocalDate valueDate;
}

package com.tradepositiontracker.dto;

import com.tradepositiontracker.enums.PositionAction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PositionHistoryResponse {

    private Long id;
    private String party;
    private String currency;
    private LocalDate valueDate;
    private String tradeReference;
    private PositionAction action;
    private BigDecimal previousExposure;
    private BigDecimal updatedExposure;
    private BigDecimal previousObligation;
    private BigDecimal updatedObligation;
    private BigDecimal previousNetPosition;
    private BigDecimal updatedNetPosition;
    private LocalDateTime timestamp;
}

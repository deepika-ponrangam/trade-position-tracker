package com.tradepositiontracker.dto;

import com.tradepositiontracker.enums.PositionAction;
import com.tradepositiontracker.model.PositionHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    public static PositionHistoryResponse fromEntity(PositionHistory history) {
        if (history == null) return null;
        return PositionHistoryResponse.builder()
                .id(history.getId())
                .party(history.getParty())
                .currency(history.getCurrency())
                .valueDate(history.getValueDate())
                .tradeReference(history.getTradeReference())
                .action(history.getAction())
                .previousExposure(history.getPreviousExposure())
                .updatedExposure(history.getUpdatedExposure())
                .previousObligation(history.getPreviousObligation())
                .updatedObligation(history.getUpdatedObligation())
                .previousNetPosition(history.getPreviousNetPosition())
                .updatedNetPosition(history.getUpdatedNetPosition())
                .timestamp(history.getTimestamp())
                .build();
    }
}

package com.tradepositiontracker.dto;

import com.tradepositiontracker.enums.TradeAction;
import com.tradepositiontracker.enums.TradeStatus;
import com.tradepositiontracker.model.TradeHistory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TradeHistoryResponse {
    private String tradeReference;
    private TradeAction action;
    private TradeStatus previousStatus;
    private TradeStatus updatedStatus;
    private BigDecimal previousPrimaryAmount;
    private BigDecimal updatedPrimaryAmount;
    private BigDecimal previousSecondaryAmount;
    private BigDecimal updatedSecondaryAmount;
    private String updatedBy;
    private LocalDateTime timestamp;

    public static TradeHistoryResponse fromEntity(TradeHistory history) {
        if (history == null) return null;
        
        return TradeHistoryResponse.builder()
                .tradeReference(history.getTradeReference())
                .action(history.getAction())
                .previousStatus(history.getPreviousStatus())
                .updatedStatus(history.getUpdatedStatus())
                .previousPrimaryAmount(history.getPreviousPrimaryAmount())
                .updatedPrimaryAmount(history.getUpdatedPrimaryAmount())
                .previousSecondaryAmount(history.getPreviousSecondaryAmount())
                .updatedSecondaryAmount(history.getUpdatedSecondaryAmount())
                .updatedBy(history.getUpdatedBy())
                .timestamp(history.getTimestamp())
                .build();
    }
}

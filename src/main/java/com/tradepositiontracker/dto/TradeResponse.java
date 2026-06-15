package com.tradepositiontracker.dto;

import com.tradepositiontracker.enums.Direction;
import com.tradepositiontracker.enums.TradeStatus;
import com.tradepositiontracker.model.Trade;

import lombok.Builder;
import lombok.Data;
import com.tradepositiontracker.util.CurrencyFormatter;

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

    public static TradeResponse fromEntity(Trade trade){
        if (trade == null){
            return null;

        }
        return TradeResponse.builder()
               .id(trade.getId())
               .tradeReference(trade.getTradeReference())
               .tradingParty(trade.getTradingParty())
               .counterParty(trade.getCounterParty())
               .primaryCurrency(trade.getPrimaryCurrency())
               .primaryAmount(CurrencyFormatter.format(trade.getPrimaryAmount(),trade.getPrimaryCurrency()))
               .secondaryCurrency(trade.getSecondaryCurrency())
               .secondaryAmount(CurrencyFormatter.format(trade.getSecondaryAmount(), trade.getSecondaryCurrency()))
               .direction(trade.getDirection())
               .valueDate(trade.getValueDate())
               .tradeDate(trade.getTradeDate())
               .status(trade.getStatus())
               .settledAt(trade.getSettledAt())
               .createdAt(trade.getCreatedAt())
               .updatedAt(trade.getUpdatedAt())
               .build();
    }
}

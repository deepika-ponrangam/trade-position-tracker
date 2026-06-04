package com.tradepositiontracker.controller;

import com.tradepositiontracker.dto.TradeResponse;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PutMapping("/{tradeReference}/match")
    public ResponseEntity<TradeResponse> matchTrade(@PathVariable String tradeReference) {
        return ResponseEntity.ok(toResponse(settlementService.matchTrade(tradeReference)));
    }

    @PutMapping("/{tradeReference}/settle")
    public ResponseEntity<TradeResponse> settleTrade(@PathVariable String tradeReference) {
        return ResponseEntity.ok(toResponse(settlementService.settleTrade(tradeReference)));
    }

    @PutMapping("/{tradeReference}/cancel")
    public ResponseEntity<TradeResponse> cancelTrade(@PathVariable String tradeReference) {
        return ResponseEntity.ok(toResponse(settlementService.cancelTrade(tradeReference)));
    }

    private TradeResponse toResponse(Trade trade) {
        return TradeResponse.builder()
                .id(trade.getId())
                .tradeReference(trade.getTradeReference())
                .tradingParty(trade.getTradingParty())
                .counterParty(trade.getCounterParty())
                .primaryCurrency(trade.getPrimaryCurrency())
                .primaryAmount(trade.getPrimaryAmount())
                .secondaryCurrency(trade.getSecondaryCurrency())
                .secondaryAmount(trade.getSecondaryAmount())
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

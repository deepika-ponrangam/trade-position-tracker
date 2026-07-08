package com.tradepositiontracker.controller;

import com.tradepositiontracker.dto.TradeResponse;
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
        return ResponseEntity.ok(settlementService.matchTrade(tradeReference));
    }

    @PutMapping("/{tradeReference}/settle")
    public ResponseEntity<TradeResponse> settleTrade(@PathVariable String tradeReference) {
        return ResponseEntity.ok(settlementService.settleTrade(tradeReference));
    }

    @PutMapping("/{tradeReference}/cancel")
    public ResponseEntity<TradeResponse> cancelTrade(@PathVariable String tradeReference) {
        return ResponseEntity.ok(settlementService.cancelTrade(tradeReference));
    }
}
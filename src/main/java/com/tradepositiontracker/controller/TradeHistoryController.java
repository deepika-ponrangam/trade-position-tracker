package com.tradepositiontracker.controller;

import com.tradepositiontracker.model.TradeHistory;
import com.tradepositiontracker.service.TradeHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeHistoryController {
    private final TradeHistoryService tradeHistoryService;

    @GetMapping("/{tradeReference}/history")
    public ResponseEntity<List<TradeHistory>> getTradeHistory(@PathVariable String tradeReference) {
        List<TradeHistory> history = tradeHistoryService.getTradeHistoryByTradeReference(tradeReference);
        return ResponseEntity.ok(history);
    }
}

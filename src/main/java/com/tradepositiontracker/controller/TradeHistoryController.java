package com.tradepositiontracker.controller;

import com.tradepositiontracker.dto.TradeHistoryResponse;
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
    public ResponseEntity<List<TradeHistoryResponse>> getTradeHistory(@PathVariable String tradeReference) {
        return ResponseEntity.ok(tradeHistoryService.getHistoryByTradeReference(tradeReference));
    }
}

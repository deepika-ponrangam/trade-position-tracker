package com.tradepositiontracker.controller;

import com.tradepositiontracker.dto.TradeRequest;
import com.tradepositiontracker.dto.TradeResponse;
import com.tradepositiontracker.enums.TradeStatus;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public ResponseEntity<TradeResponse> bookTrade(@RequestBody TradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tradeService.bookTrade(request));
    }

    @PutMapping("/{tradeReference}/match")
    public ResponseEntity<TradeResponse> matchTrade(@PathVariable String tradeReference) {
        return ResponseEntity.ok(tradeService.matchTrade(tradeReference));
    }

    @PutMapping("/{tradeReference}/settle")
    public ResponseEntity<TradeResponse> settleTrade(@PathVariable String tradeReference) {
        return ResponseEntity.ok(tradeService.settleTrade(tradeReference));
    }

    @PutMapping("/{tradeReference}/cancel")
    public ResponseEntity<TradeResponse> cancelTrade(@PathVariable String tradeReference) {
        return ResponseEntity.ok(tradeService.cancelTrade(tradeReference));
    }
    
    @GetMapping
    public ResponseEntity<Page<TradeResponse>> getAllTrades(
            @RequestParam(required = false) TradeStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        if (status != null) {
            return ResponseEntity.ok(tradeService.getTradesByStatus(status, pageable));
        } 
        return ResponseEntity.ok(tradeService.getAllTrades(pageable));
    }

    @GetMapping("/{tradeReference}")
    public ResponseEntity<TradeResponse> getTrade(@PathVariable String tradeReference) {
        return ResponseEntity.ok(tradeService.getTrade(tradeReference));
    }

    @PutMapping("/{tradeReference}/amend")
    public ResponseEntity<TradeResponse> amendTrade(@PathVariable String tradeReference,
                                                     @RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradeService.amendTrade(tradeReference, request));
    }
}

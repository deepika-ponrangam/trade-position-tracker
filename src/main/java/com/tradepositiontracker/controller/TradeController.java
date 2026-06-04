package com.tradepositiontracker.controller;

import com.tradepositiontracker.model.Position;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    @Autowired
    private PositionService positionService;

    @PostMapping
    public ResponseEntity<Position> submitTrade(@RequestBody Trade trade) {
        Position updatedPosition = positionService.processTrade(trade);
        return ResponseEntity.ok(updatedPosition);
    }
}

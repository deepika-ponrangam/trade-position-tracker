package com.tradepositiontracker.controller;

import com.tradepositiontracker.model.Position;
import com.tradepositiontracker.service.PositionService;
import com.tradepositiontracker.service.ProfitAndLossService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @Autowired
    private ProfitAndLossService profitAndLossService;

    @GetMapping
    public ResponseEntity<List<Position>> getAllPositions() {
        return ResponseEntity.ok(positionService.getAllPositions());
    }

    @GetMapping("/{tradingParty}")
    public ResponseEntity<List<Position>> getPositionsByTradingParty(@PathVariable String tradingParty) {
        return ResponseEntity.ok(positionService.getPositionsByTradingParty(tradingParty));
    }

    @GetMapping("/{tradingParty}/{instrument}")
    public ResponseEntity<Map<String, Object>> getPosition(
            @PathVariable String tradingParty, @PathVariable String instrument) {
        Position position = positionService.getPosition(tradingParty, instrument);

        Map<String, Object> response = new HashMap<>();
        response.put("position", position);

        try {
            double unrealizedProfitAndLoss = profitAndLossService.calculateUnrealizedProfitAndLoss(position);
            response.put("unrealizedProfitAndLoss", unrealizedProfitAndLoss);
        } catch (IllegalArgumentException e) {
            response.put("unrealizedProfitAndLoss", "Market price not available");
        }

        return ResponseEntity.ok(response);
    }
}

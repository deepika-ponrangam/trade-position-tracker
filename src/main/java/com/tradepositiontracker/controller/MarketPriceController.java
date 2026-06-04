package com.tradepositiontracker.controller;

import com.tradepositiontracker.model.MarketPrice;
import com.tradepositiontracker.service.MarketPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market-prices")
public class MarketPriceController {

    @Autowired
    private MarketPriceService marketPriceService;

    @PostMapping
    public ResponseEntity<MarketPrice> updatePrice(@RequestBody MarketPrice marketPrice) {
        MarketPrice updated = marketPriceService.updatePrice(
                marketPrice.getInstrument(), marketPrice.getPrice());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{instrument}")
    public ResponseEntity<MarketPrice> getPrice(@PathVariable String instrument) {
        return ResponseEntity.ok(marketPriceService.getPrice(instrument));
    }
}

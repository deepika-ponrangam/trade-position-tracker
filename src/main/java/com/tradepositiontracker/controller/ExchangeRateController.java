package com.tradepositiontracker.controller;

import com.tradepositiontracker.dto.ExchangeRateRequest;
import com.tradepositiontracker.dto.ExchangeRateResponse;
import com.tradepositiontracker.model.ExchangeRate;
import com.tradepositiontracker.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping
    public ResponseEntity<ExchangeRateResponse> updateRate(@RequestBody ExchangeRateRequest request) {
        ExchangeRate updated = exchangeRateService.updateRate(request.getCurrency(), request.getRateToUsd());
        return ResponseEntity.ok(toResponse(updated));
    }

    @GetMapping("/{currency}")
    public ResponseEntity<ExchangeRateResponse> getRate(@PathVariable String currency) {
        return ResponseEntity.ok(toResponse(exchangeRateService.getRate(currency)));
    }

    private ExchangeRateResponse toResponse(ExchangeRate rate) {
        return ExchangeRateResponse.builder()
                .id(rate.getId())
                .currency(rate.getCurrency())
                .rateToUsd(rate.getRateToUsd())
                .updatedAt(rate.getUpdatedAt())
                .build();
    }
}

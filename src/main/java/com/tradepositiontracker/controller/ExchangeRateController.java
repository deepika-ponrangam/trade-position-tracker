package com.tradepositiontracker.controller;

import com.tradepositiontracker.dto.ExchangeRateAuditResponse;
import com.tradepositiontracker.dto.ExchangeRateRequest;
import com.tradepositiontracker.dto.ExchangeRateResponse;
import com.tradepositiontracker.model.ExchangeRate;
import com.tradepositiontracker.service.ExchangeRateAuditService;
import com.tradepositiontracker.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tradepositiontracker.util.CurrencyValidator;
import java.util.List;
@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateAuditService exchangeRateAuditService;

    @PostMapping
    public ResponseEntity<ExchangeRateResponse> updateRate(@RequestBody ExchangeRateRequest request) {
        CurrencyValidator.validate(request.getCurrency());
        ExchangeRate updated = exchangeRateService.updateRate(request.getCurrency(), request.getRateToUsd());
        return ResponseEntity.ok(toResponse(updated));
    }

    @GetMapping("/{currency}")
    public ResponseEntity<ExchangeRateResponse> getRate(@PathVariable String currency) {
        CurrencyValidator.validate(currency);
        return ResponseEntity.ok(toResponse(exchangeRateService.getRate(currency)));
    }

    @GetMapping("/{currency}/audit")
    public ResponseEntity<List<ExchangeRateAuditResponse>> getRateHistory(@PathVariable String currency) {
        CurrencyValidator.validate(currency);
        List<ExchangeRateAuditResponse> history = exchangeRateAuditService.getExchangeRateAuditHistory(currency);
        
        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(history);
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

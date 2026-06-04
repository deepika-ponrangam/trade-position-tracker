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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public ResponseEntity<TradeResponse> bookTrade(@RequestBody TradeRequest request) {
        Trade booked = tradeService.bookTrade(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(booked));
    }

    @GetMapping
    public ResponseEntity<Page<TradeResponse>> getAllTrades(
            @RequestParam(required = false) TradeStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<Trade> trades;
        if (status != null) {
            trades = tradeService.getTradesByStatus(status, pageable);
        } else {
            trades = tradeService.getAllTrades(pageable);
        }
        return ResponseEntity.ok(trades.map(this::toResponse));
    }

    @GetMapping("/{tradeReference}")
    public ResponseEntity<TradeResponse> getTrade(@PathVariable String tradeReference) {
        return ResponseEntity.ok(toResponse(tradeService.getTrade(tradeReference)));
    }

    @PutMapping("/{tradeReference}/amend")
    public ResponseEntity<TradeResponse> amendTrade(@PathVariable String tradeReference,
                                                     @RequestBody TradeRequest request) {
        Trade amended = tradeService.amendTrade(tradeReference, toEntity(request));
        return ResponseEntity.ok(toResponse(amended));
    }

    private Trade toEntity(TradeRequest request) {
        Trade trade = new Trade();
        trade.setTradeReference(request.getTradeReference());
        trade.setTradingParty(request.getTradingParty());
        trade.setCounterParty(request.getCounterParty());
        trade.setPrimaryCurrency(request.getPrimaryCurrency());
        trade.setPrimaryAmount(request.getPrimaryAmount());
        trade.setSecondaryCurrency(request.getSecondaryCurrency());
        trade.setSecondaryAmount(request.getSecondaryAmount());
        trade.setDirection(request.getDirection());
        trade.setValueDate(request.getValueDate());
        return trade;
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

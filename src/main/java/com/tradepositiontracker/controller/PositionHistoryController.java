package com.tradepositiontracker.controller;

import com.tradepositiontracker.dto.PositionHistoryResponse;
import com.tradepositiontracker.model.PositionHistory;
import com.tradepositiontracker.service.PositionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionHistoryController {

    private final PositionHistoryService positionHistoryService;

    @GetMapping("/{party}/history")
    public ResponseEntity<List<PositionHistoryResponse>> getHistory(@PathVariable String party) {
        return ResponseEntity.ok(
                positionHistoryService.getHistoryByParty(party)
                        .stream().map(this::toResponse).toList());
    }

    @GetMapping("/{party}/{currency}/history")
    public ResponseEntity<List<PositionHistoryResponse>> getHistoryByCurrency(
            @PathVariable String party, @PathVariable String currency) {
        return ResponseEntity.ok(
                positionHistoryService.getHistoryByPartyAndCurrency(party, currency)
                        .stream().map(this::toResponse).toList());
    }

    private PositionHistoryResponse toResponse(PositionHistory history) {
        return PositionHistoryResponse.builder()
                .id(history.getId())
                .party(history.getParty())
                .currency(history.getCurrency())
                .valueDate(history.getValueDate())
                .tradeReference(history.getTradeReference())
                .action(history.getAction())
                .previousExposure(history.getPreviousExposure())
                .updatedExposure(history.getUpdatedExposure())
                .previousObligation(history.getPreviousObligation())
                .updatedObligation(history.getUpdatedObligation())
                .previousNetPosition(history.getPreviousNetPosition())
                .updatedNetPosition(history.getUpdatedNetPosition())
                .timestamp(history.getTimestamp())
                .build();
    }
}

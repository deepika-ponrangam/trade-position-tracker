package com.tradepositiontracker.controller;

import com.tradepositiontracker.dto.PositionResponse;
import com.tradepositiontracker.model.Position;
import com.tradepositiontracker.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping("/{party}")
    public ResponseEntity<List<PositionResponse>> getPositions(
            @PathVariable String party,
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<Position> positions;
        if (bucket != null) {
            positions = positionService.getPositionsByPartyAndBucket(party, bucket);
        } else if (from != null && to != null) {
            positions = positionService.getPositionsByPartyAndDateRange(party, from, to);
        } else {
            positions = positionService.getPositionsByParty(party);
        }
        return ResponseEntity.ok(positions.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{party}/{currency}")
    public ResponseEntity<List<PositionResponse>> getPositionsByCurrency(
            @PathVariable String party, @PathVariable String currency) {
        return ResponseEntity.ok(
                positionService.getPositionsByPartyAndCurrency(party, currency)
                        .stream().map(this::toResponse).toList());
    }

    private PositionResponse toResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .party(position.getParty())
                .currency(position.getCurrency())
                .valueDate(position.getValueDate())
                .exposure(position.getExposure())
                .obligation(position.getObligation())
                .netPosition(position.getNetPosition())
                .usdEquivalent(position.getUsdEquivalent())
                .build();
    }
}

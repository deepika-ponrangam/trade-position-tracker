package com.tradepositiontracker.controller;

import com.tradepositiontracker.dto.PositionAuditResponse;
import com.tradepositiontracker.dto.PositionResponse;
import com.tradepositiontracker.service.PositionAuditService;
import com.tradepositiontracker.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tradepositiontracker.util.CurrencyValidator;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;
    private final PositionAuditService positionAuditService;

    @GetMapping
    public ResponseEntity<Page<PositionResponse>> getAllPositions(
            @PageableDefault(size = 20, sort = "party") Pageable pageable) {
        return ResponseEntity.ok(positionService.getAllPositions(pageable));
    }

    @GetMapping("/{party}")
    public ResponseEntity<List<PositionResponse>> getPositions(
            @PathVariable String party,
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<PositionResponse> positions;
        
        if (bucket != null) {
            positions = positionService.getPositionsByPartyAndBucket(party, bucket);
        } else if (from != null && to != null) {
            positions = positionService.getPositionsByPartyAndDateRange(party, from, to);
        } else {
            positions = positionService.getPositionsByParty(party);
        }
        
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/{party}/{currency}")
    public ResponseEntity<List<PositionResponse>> getPosition(
            @PathVariable String party,
            @PathVariable String currency) {
        CurrencyValidator.validate(currency);
        return ResponseEntity.ok(positionService.getPosition(party, currency));
    }
    @GetMapping("/{id}/audit")
    public ResponseEntity<List<PositionAuditResponse>> getPositionAuditHistory(@PathVariable Long id) {
        List<PositionAuditResponse> history = positionAuditService.getPositionAuditHistory(id);

        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(history);
    }
}
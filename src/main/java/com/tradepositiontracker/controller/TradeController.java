package com.tradepositiontracker.controller;

import com.tradepositiontracker.dto.TradeRequest;
import com.tradepositiontracker.dto.TradeResponse;
import com.tradepositiontracker.enums.TradeStatus;
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
import com.tradepositiontracker.service.TradeAuditService;
import java.util.List;
import com.tradepositiontracker.dto.TradeAuditResponse;
@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final TradeAuditService tradeAuditService;

    @PostMapping
    public ResponseEntity<TradeResponse> bookTrade(@RequestBody TradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tradeService.bookTrade(request));
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
    @GetMapping("/{tradeReference}/audit")
    public ResponseEntity<List<TradeAuditResponse>> getTradeHistory(@PathVariable String tradeReference){
        List<TradeAuditResponse> history = tradeAuditService.getTradeAuditHistory(tradeReference);

        if(history.isEmpty()){
            return ResponseEntity.notFound().build();
        
        }
        return ResponseEntity.ok(history);
    }
}

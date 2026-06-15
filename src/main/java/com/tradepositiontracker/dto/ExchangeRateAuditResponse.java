package com.tradepositiontracker.dto;
import lombok.Builder;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder

public class ExchangeRateAuditResponse {
    private LocalDateTime changedAt;
    private String revisionType;
    private Set<String> changedFields;
    private ExchangeRateResponse rateSnapshot;    
}

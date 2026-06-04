package com.tradepositiontracker.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PositionResponse {

    private Long id;
    private String party;
    private String currency;
    private LocalDate valueDate;
    private String exposure;
    private String obligation;
    private String netPosition;
    private String usdEquivalent;
}

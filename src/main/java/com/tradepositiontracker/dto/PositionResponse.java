package com.tradepositiontracker.dto;
import com.tradepositiontracker.model.Position;
import com.tradepositiontracker.util.CurrencyFormatter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponse {

    private Long id;
    private String party;
    private String currency;
    private LocalDate valueDate;
    private String exposure;
    private String obligation;
    private String netPosition;
    private String usdEquivalent;

    public static PositionResponse fromEntity(Position position, BigDecimal usdEquivalent) {
        if (position == null){
            return null;
        }
        return PositionResponse.builder()
               .id(position.getId())
               .party(position.getParty())
               .currency(position.getCurrency())
               .valueDate(position.getValueDate())
               .exposure(formatToString(position.getExposure(), position.getCurrency()))
               .obligation(formatToString(position.getObligation(), position.getCurrency()))
               .netPosition(formatToString(position.getNetPosition(), position.getCurrency()))
               .usdEquivalent(usdEquivalent != null ? formatToString(usdEquivalent, "USD"): "N/A")
               .build();
    }
    private static String formatToString(BigDecimal amount, String currencyCode){
        if (amount == null){
            return null;
        }
        BigDecimal formattedAmount = CurrencyFormatter.format(amount, currencyCode);
        return formattedAmount.toPlainString();
    }
}
package com.tradepositiontracker.service;

import com.tradepositiontracker.dto.TradeRequest;
import com.tradepositiontracker.dto.TradeResponse;
import com.tradepositiontracker.enums.TradeStatus;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tradepositiontracker.util.CurrencyFormatter;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final TradeValidationService tradeValidationService;
    private final PositionService positionService;

    @Transactional
    public TradeResponse bookTrade(TradeRequest request) {
        Trade trade = toEntity(request);

        normalizeTradeFields(trade);
        tradeValidationService.validateNewTrade(trade);

        trade.setTradeDate(LocalDate.now());
        trade.setStatus(TradeStatus.PENDING);
        Trade savedTrade = tradeRepository.save(trade);

        positionService.updatePositionsForNewTrade(savedTrade);
        return toResponse(savedTrade);
    }

    @Transactional
    public TradeResponse amendTrade(String tradeReference, TradeRequest request) {
        Trade existingTrade = tradeRepository.findByTradeReference(tradeReference)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeReference));

        if (existingTrade.getStatus() != TradeStatus.PENDING && existingTrade.getStatus() != TradeStatus.MATCHED) {
            throw new IllegalArgumentException("Only PENDING or MATCHED trades can be amended");
        }
        Trade amendment = toEntity(request);

        normalizeTradeFields(amendment);
        tradeValidationService.validateAmendment(amendment);
        positionService.reversePositionsForTrade(existingTrade);

        existingTrade.setTradingParty(amendment.getTradingParty());
        existingTrade.setCounterParty(amendment.getCounterParty());
        existingTrade.setPrimaryCurrency(amendment.getPrimaryCurrency());
        existingTrade.setPrimaryAmount(amendment.getPrimaryAmount());
        existingTrade.setSecondaryCurrency(amendment.getSecondaryCurrency());
        existingTrade.setSecondaryAmount(amendment.getSecondaryAmount());
        existingTrade.setDirection(amendment.getDirection());
        existingTrade.setValueDate(amendment.getValueDate());

        Trade savedTrade = tradeRepository.save(existingTrade);
        positionService.updatePositionsForNewTrade(savedTrade);
        return toResponse(savedTrade);
    }
    public TradeResponse getTrade(String tradeReference){
        Trade trade = tradeRepository.findByTradeReference(tradeReference)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found" + tradeReference));
        return toResponse(trade);
    }
    public Page<TradeResponse> getAllTrades(Pageable pageable){
        return tradeRepository.findAll(pageable).map(this::toResponse);
    }
    public Page<TradeResponse> getTradesByStatus(TradeStatus status, Pageable pageable) {
        return tradeRepository.findByStatus(status, pageable).map(this::toResponse);
    }
    private void normalizeTradeFields(Trade trade) {
        if (trade.getTradingParty() != null) {
            trade.setTradingParty(trade.getTradingParty().trim().toUpperCase());
        }
        if (trade.getCounterParty() != null) {
            trade.setCounterParty(trade.getCounterParty().trim().toUpperCase());
        }
        if (trade.getPrimaryCurrency() != null) {
            trade.setPrimaryCurrency(trade.getPrimaryCurrency().trim().toUpperCase());
        }
        if (trade.getSecondaryCurrency() != null) {
            trade.setSecondaryCurrency(trade.getSecondaryCurrency().trim().toUpperCase());
        }
        if (trade.getTradeReference() != null) {
            trade.setTradeReference(trade.getTradeReference().trim());
        }
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
                .primaryAmount(CurrencyFormatter.format(trade.getPrimaryAmount(), trade.getPrimaryCurrency()))
                .secondaryCurrency(trade.getSecondaryCurrency())
                .secondaryAmount(CurrencyFormatter.format(trade.getSecondaryAmount(), trade.getSecondaryCurrency()))
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

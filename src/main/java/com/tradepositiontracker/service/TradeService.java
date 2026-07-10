package com.tradepositiontracker.service;

import com.tradepositiontracker.dto.TradeRequest;
import com.tradepositiontracker.dto.TradeResponse;
import com.tradepositiontracker.enums.TradeAction;
import com.tradepositiontracker.enums.TradeStatus;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tradepositiontracker.util.CurrencyFormatter;
import com.tradepositiontracker.message.TradeMessageProducer;
import com.tradepositiontracker.service.TradeHistoryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final TradeValidationService tradeValidationService;
    private final PositionService positionService;
    private final TradeMessageProducer tradeMessageProducer;
    private final TradeHistoryService tradeHistoryService;

    @Transactional
    public TradeResponse bookTrade(TradeRequest request) {
        Trade trade = toEntity(request);

        normalizeTradeFields(trade);
        tradeValidationService.validateNewTrade(trade);

        trade.setTradeDate(LocalDate.now());
        trade.setStatus(TradeStatus.PENDING);
        Trade savedTrade = tradeRepository.save(trade);
        tradeHistoryService.recordChange(savedTrade, TradeAction.STATUS_CHANGED, null, BigDecimal.ZERO, BigDecimal.ZERO);
        TradeResponse response = TradeResponse.fromEntity(savedTrade);
        tradeMessageProducer.sendTradeUpdate(response);
        return response;
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
        TradeStatus oldStatus = existingTrade.getStatus();
        BigDecimal oldPrimaryAmount = existingTrade.getPrimaryAmount();
        BigDecimal oldSecondaryAmount = existingTrade.getSecondaryAmount();
        if (oldStatus == TradeStatus.MATCHED) {
            positionService.reversePositionsForTrade(existingTrade);
        }
        existingTrade.setTradingParty(amendment.getTradingParty());
        existingTrade.setCounterParty(amendment.getCounterParty());
        existingTrade.setPrimaryCurrency(amendment.getPrimaryCurrency());
        existingTrade.setPrimaryAmount(amendment.getPrimaryAmount());
        existingTrade.setSecondaryCurrency(amendment.getSecondaryCurrency());
        existingTrade.setSecondaryAmount(amendment.getSecondaryAmount());
        existingTrade.setDirection(amendment.getDirection());
        existingTrade.setValueDate(amendment.getValueDate());

        existingTrade.setStatus(TradeStatus.PENDING);

        Trade savedTrade = tradeRepository.save(existingTrade);
        tradeHistoryService.recordChange(savedTrade, TradeAction.TRADE_AMENDED, oldStatus, oldPrimaryAmount, oldSecondaryAmount);
        TradeResponse response = TradeResponse.fromEntity(savedTrade);
        tradeMessageProducer.sendTradeUpdate(response);
        return response;
    }
    @Transactional
    public TradeResponse matchTrade(String tradeReference){
        Trade trade = getTradeEntity(tradeReference);
        if(trade.getStatus()!= TradeStatus.PENDING){
            throw new IllegalArgumentException("Only PENDING trades can be matched.");
        }
        TradeStatus oldStatus = trade.getStatus();
        trade.setStatus(TradeStatus.MATCHED);
        Trade savedTrade = tradeRepository.save(trade);
        positionService.updatePositionsForNewTrade(savedTrade);
        tradeHistoryService.recordChange(savedTrade, TradeAction.STATUS_CHANGED, oldStatus, savedTrade.getPrimaryAmount(), savedTrade.getSecondaryAmount());
        TradeResponse response = TradeResponse.fromEntity(savedTrade);
        tradeMessageProducer.sendTradeUpdate(response);
        return response;
    }
    @Transactional
    public TradeResponse settleTrade(String tradeReference){
        Trade trade = getTradeEntity(tradeReference);
        if(trade.getStatus()!= TradeStatus.MATCHED){
            throw new IllegalArgumentException("Only MATCHED trades can be settled.");
        }
        TradeStatus oldStatus = trade.getStatus();
        trade.setStatus(TradeStatus.SETTLED);
        trade.setSettledAt(LocalDateTime.now());
        Trade savedTrade = tradeRepository.save(trade);
        positionService.settlePositionsForTrade(savedTrade);
        tradeHistoryService.recordChange(savedTrade, TradeAction.STATUS_CHANGED, oldStatus, savedTrade.getPrimaryAmount(), savedTrade.getSecondaryAmount());
        TradeResponse response = TradeResponse.fromEntity(savedTrade);
        tradeMessageProducer.sendTradeUpdate(response);
        return response;
    }
    @Transactional
    public TradeResponse cancelTrade(String tradeReference) {
        Trade trade = getTradeEntity(tradeReference);
        if (trade.getStatus() != TradeStatus.PENDING && trade.getStatus() != TradeStatus.MATCHED) {
            throw new IllegalArgumentException("Only PENDING or MATCHED trades can be cancelled.");
        }
        
        TradeStatus oldStatus = trade.getStatus();
        trade.setStatus(TradeStatus.CANCELLED);
        Trade savedTrade = tradeRepository.save(trade);

        if (oldStatus == TradeStatus.MATCHED) {
            positionService.reversePositionsForTrade(savedTrade);
        }

        tradeHistoryService.recordChange(savedTrade, TradeAction.STATUS_CHANGED, oldStatus, savedTrade.getPrimaryAmount(), savedTrade.getSecondaryAmount());
        
        TradeResponse response = TradeResponse.fromEntity(savedTrade);
        tradeMessageProducer.sendTradeUpdate(response);
        return response;
    }

    public TradeResponse getTrade(String tradeReference){
        return TradeResponse.fromEntity(getTradeEntity(tradeReference));
    }
    public Page<TradeResponse> getAllTrades(Pageable pageable){
        return tradeRepository.findAll(pageable).map(TradeResponse::fromEntity);
    }
    public Page<TradeResponse> getTradesByStatus(TradeStatus status, Pageable pageable) {
        return tradeRepository.findByStatus(status, pageable).map(TradeResponse::fromEntity);
    }
    private Trade getTradeEntity(String tradeReference) {
        return tradeRepository.findByTradeReference(tradeReference)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeReference));
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
}

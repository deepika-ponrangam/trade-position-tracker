package com.tradepositiontracker.service;

import com.tradepositiontracker.enums.TradeAction;
import com.tradepositiontracker.enums.TradeStatus;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final TradeRepository tradeRepository;
    private final PositionService positionService;
    private final TradeHistoryService tradeHistoryService;
    @Transactional
    public Trade matchTrade(String tradeReference) {
        Trade trade = findTradeByReference(tradeReference);

        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING trades can be matched. Current status: " + trade.getStatus());
        }
        TradeStatus oldStatus = trade.getStatus();
        trade.setStatus(TradeStatus.MATCHED);
        Trade savedTrade = tradeRepository.save(trade);
        tradeHistoryService.recordChange(savedTrade, TradeAction.STATUS_CHANGED, oldStatus, savedTrade.getPrimaryAmount(), savedTrade.getSecondaryAmount());
        return savedTrade;
    }

    @Transactional
    public Trade settleTrade(String tradeReference) {
        Trade trade = findTradeByReference(tradeReference);

        if (trade.getStatus() != TradeStatus.MATCHED) {
            throw new IllegalArgumentException(
                    "Only MATCHED trades can be settled. Current status: " + trade.getStatus());
        }

        TradeStatus oldStatus = trade.getStatus();
        trade.setStatus(TradeStatus.SETTLED);
        trade.setSettledAt(LocalDateTime.now());
        Trade savedTrade = tradeRepository.save(trade);
        tradeHistoryService.recordChange(savedTrade, TradeAction.STATUS_CHANGED, oldStatus, savedTrade.getPrimaryAmount(), savedTrade.getSecondaryAmount());
        return savedTrade;
    }

    @Transactional
    public Trade cancelTrade(String tradeReference) {
        Trade trade = findTradeByReference(tradeReference);

        if (trade.getStatus() != TradeStatus.PENDING && trade.getStatus() != TradeStatus.MATCHED) {
            throw new IllegalArgumentException(
                    "Only PENDING or MATCHED trades can be cancelled. Current status: " + trade.getStatus());
        }

        TradeStatus oldStatus = trade.getStatus();
        trade.setStatus(TradeStatus.CANCELLED);
        Trade savedTrade = tradeRepository.save(trade);
        tradeHistoryService.recordChange(savedTrade, TradeAction.STATUS_CHANGED, oldStatus, savedTrade.getPrimaryAmount(), savedTrade.getSecondaryAmount());
        return savedTrade;
    }

    private Trade findTradeByReference(String tradeReference) {
        return tradeRepository.findByTradeReference(tradeReference)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeReference));
    }
}

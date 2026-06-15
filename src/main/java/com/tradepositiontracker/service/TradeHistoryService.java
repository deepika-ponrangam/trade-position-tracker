package com.tradepositiontracker.service;
import com.tradepositiontracker.enums.TradeAction;
import com.tradepositiontracker.enums.TradeStatus;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.model.TradeHistory;
import com.tradepositiontracker.repository.TradeHistoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeHistoryService {
    private final TradeHistoryRepository tradeHistoryRepository;
    public void recordChange(Trade trade, TradeAction action,
        TradeStatus previousStatus, BigDecimal previousPrimary,
        BigDecimal previousSecondary) {
        TradeHistory history = new TradeHistory();
        history.setTradeReference(trade.getTradeReference());
        history.setAction(action);
        history.setPreviousStatus(previousStatus);
        history.setUpdatedStatus(trade.getStatus());
        history.setPreviousPrimaryAmount(previousPrimary);
        history.setUpdatedPrimaryAmount(trade.getPrimaryAmount());
        history.setPreviousSecondaryAmount(previousSecondary);
        history.setUpdatedSecondaryAmount(trade.getSecondaryAmount());
        tradeHistoryRepository.save(history);
        }
        public List<TradeHistory> getHistoryByTradeReference(String tradeReference){
            return tradeHistoryRepository.findByTradeReferenceOrderByTimestampDesc(tradeReference);
        }
}

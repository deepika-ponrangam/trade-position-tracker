package com.tradepositiontracker.service;

import com.tradepositiontracker.enums.TradeStatus;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final TradeValidationService tradeValidationService;
    private final PositionService positionService;

    @Transactional
    public Trade bookTrade(Trade trade) {
        normalizeTradeFields(trade);
        tradeValidationService.validateNewTrade(trade);

        trade.setTradeDate(LocalDate.now());
        trade.setStatus(TradeStatus.PENDING);
        Trade savedTrade = tradeRepository.save(trade);

        positionService.updatePositionsForNewTrade(savedTrade);
        return savedTrade;
    }

    @Transactional
    public Trade amendTrade(String tradeReference, Trade amendment) {
        Trade existingTrade = tradeRepository.findByTradeReference(tradeReference)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeReference));

        if (existingTrade.getStatus() != TradeStatus.PENDING && existingTrade.getStatus() != TradeStatus.MATCHED) {
            throw new IllegalArgumentException("Only PENDING or MATCHED trades can be amended");
        }

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
        return savedTrade;
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

    public Trade getTrade(String tradeReference) {
        return tradeRepository.findByTradeReference(tradeReference)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeReference));
    }

    public Page<Trade> getAllTrades(Pageable pageable) {
        return tradeRepository.findAll(pageable);
    }

    public Page<Trade> getTradesByStatus(TradeStatus status, Pageable pageable) {
        return tradeRepository.findByStatus(status, pageable);
    }
}

package com.tradepositiontracker.service;

import com.tradepositiontracker.enums.Direction;
import com.tradepositiontracker.enums.PositionAction;
import com.tradepositiontracker.model.Position;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;
    private final PositionHistoryService positionHistoryService;

    public void updatePositionsForNewTrade(Trade trade) {
        BigDecimal primaryAmount = trade.getPrimaryAmount();
        BigDecimal secondaryAmount = trade.getSecondaryAmount();
        String tradeReference = trade.getTradeReference();
        LocalDate valueDate = trade.getValueDate();

        Position tradingPartyPrimaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getPrimaryCurrency(), valueDate);
        Position tradingPartySecondaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getSecondaryCurrency(), valueDate);
        Position counterPartyPrimaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getPrimaryCurrency(), valueDate);
        Position counterPartySecondaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getSecondaryCurrency(), valueDate);

        if (trade.getDirection() == Direction.BUY) {
            addExposure(tradingPartyPrimaryPosition, primaryAmount, tradeReference);
            addObligation(tradingPartySecondaryPosition, secondaryAmount, tradeReference);
            addExposure(counterPartySecondaryPosition, secondaryAmount, tradeReference);
            addObligation(counterPartyPrimaryPosition, primaryAmount, tradeReference);
        } else {
            addExposure(tradingPartyPrimaryPosition, secondaryAmount, tradeReference);
            addObligation(tradingPartySecondaryPosition, primaryAmount, tradeReference);
            addExposure(counterPartySecondaryPosition, primaryAmount, tradeReference);
            addObligation(counterPartyPrimaryPosition, secondaryAmount, tradeReference);
        }
    }

    public void reversePositionsForTrade(Trade trade) {
        BigDecimal primaryAmount = trade.getPrimaryAmount();
        BigDecimal secondaryAmount = trade.getSecondaryAmount();
        String tradeReference = trade.getTradeReference();
        LocalDate valueDate = trade.getValueDate();

        Position tradingPartyPrimaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getPrimaryCurrency(), valueDate);
        Position tradingPartySecondaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getSecondaryCurrency(), valueDate);
        Position counterPartyPrimaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getPrimaryCurrency(), valueDate);
        Position counterPartySecondaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getSecondaryCurrency(), valueDate);

        if (trade.getDirection() == Direction.BUY) {
            reduceExposure(tradingPartyPrimaryPosition, primaryAmount, tradeReference);
            reduceObligation(tradingPartySecondaryPosition, secondaryAmount, tradeReference);
            reduceExposure(counterPartySecondaryPosition, secondaryAmount, tradeReference);
            reduceObligation(counterPartyPrimaryPosition, primaryAmount, tradeReference);
        } else {
            reduceExposure(tradingPartySecondaryPosition, secondaryAmount, tradeReference);
            reduceObligation(tradingPartyPrimaryPosition, primaryAmount, tradeReference);
            reduceExposure(counterPartyPrimaryPosition, primaryAmount, tradeReference);
            reduceObligation(counterPartySecondaryPosition, secondaryAmount, tradeReference);
        }
    }

    public void settlePositionsForTrade(Trade trade) {
        BigDecimal primaryAmount = trade.getPrimaryAmount();
        BigDecimal secondaryAmount = trade.getSecondaryAmount();
        String tradeReference = trade.getTradeReference();
        LocalDate valueDate = trade.getValueDate();

        Position tradingPartyPrimaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getPrimaryCurrency(), valueDate);
        Position tradingPartySecondaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getSecondaryCurrency(), valueDate);
        Position counterPartyPrimaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getPrimaryCurrency(), valueDate);
        Position counterPartySecondaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getSecondaryCurrency(), valueDate);

        if (trade.getDirection() == Direction.BUY) {
            settlePosition(tradingPartyPrimaryPosition, primaryAmount, true, tradeReference);
            settlePosition(tradingPartySecondaryPosition, secondaryAmount, false, tradeReference);
            settlePosition(counterPartyPrimaryPosition, primaryAmount, false, tradeReference);
            settlePosition(counterPartySecondaryPosition, secondaryAmount, true, tradeReference);
        } else {
            settlePosition(tradingPartyPrimaryPosition, primaryAmount, false, tradeReference);
            settlePosition(tradingPartySecondaryPosition, secondaryAmount, true, tradeReference);
            settlePosition(counterPartyPrimaryPosition, primaryAmount, true, tradeReference);
            settlePosition(counterPartySecondaryPosition, secondaryAmount, false, tradeReference);
        }
    }

    
    private void addExposure(Position position, BigDecimal amount, String tradeReference) {
        BigDecimal prevExposure = position.getExposure();
        BigDecimal prevObligation = position.getObligation();
        BigDecimal prevNet = position.getNetPosition();

        position.setExposure(prevExposure.add(amount));
        positionRepository.save(position);

        positionHistoryService.recordChange(position, tradeReference, PositionAction.TRADE_BOOKED,
                prevExposure, prevObligation, prevNet);
    }
    private void addObligation(Position position, BigDecimal amount, String tradeReference) {
        BigDecimal prevExposure = position.getExposure();
        BigDecimal prevObligation = position.getObligation();
        BigDecimal prevNet = position.getNetPosition();

        position.setObligation(prevObligation.add(amount));
        positionRepository.save(position);

        positionHistoryService.recordChange(position, tradeReference, PositionAction.TRADE_BOOKED,
                prevExposure, prevObligation, prevNet);
    }

    private void reduceExposure(Position position, BigDecimal amount, String tradeReference) {
        BigDecimal prevExposure = position.getExposure();
        BigDecimal prevObligation = position.getObligation();
        BigDecimal prevNet = position.getNetPosition();

        position.setExposure(prevExposure.subtract(amount));
        positionRepository.save(position);

        positionHistoryService.recordChange(position, tradeReference, PositionAction.TRADE_REVERSED,
                prevExposure, prevObligation, prevNet);
    }


    private void reduceObligation(Position position, BigDecimal amount, String tradeReference) {
        BigDecimal prevExposure = position.getExposure();
        BigDecimal prevObligation = position.getObligation();
        BigDecimal prevNet = position.getNetPosition();

        position.setObligation(prevObligation.subtract(amount));
        positionRepository.save(position);

        positionHistoryService.recordChange(position, tradeReference, PositionAction.TRADE_REVERSED,
                prevExposure, prevObligation, prevNet);
    }
    private void settlePosition(Position position, BigDecimal amount, boolean isReceiving, String tradeReference) {
        BigDecimal prevExposure = position.getExposure();
        BigDecimal prevObligation = position.getObligation();
        BigDecimal prevNet = position.getNetPosition();

        if (isReceiving) {
            position.setExposure(prevExposure.subtract(amount));
            position.setNetPosition(prevNet.add(amount));
        } else {
            position.setObligation(prevObligation.subtract(amount));
            position.setNetPosition(prevNet.subtract(amount));
        }
        positionRepository.save(position);

        positionHistoryService.recordChange(position, tradeReference, PositionAction.TRADE_SETTLED,
                prevExposure, prevObligation, prevNet);
    }

    private Position getOrCreatePosition(String party, String currency, LocalDate valueDate) {
        return positionRepository.findByPartyAndCurrencyAndValueDate(party, currency, valueDate)
                .orElse(new Position(party, currency, valueDate));
    }

    public List<Position> getPositionsByParty(String party) {
        return positionRepository.findByParty(party);
    }

    public List<Position> getPositionsByPartyAndCurrency(String party, String currency) {
        return positionRepository.findByPartyAndCurrency(party, currency);
    }

    public List<Position> getPositionsByPartyAndBucket(String party, String bucket) {
        LocalDate today = LocalDate.now();
        return switch (bucket.toUpperCase()) {
            case "T0" -> positionRepository.findByPartyAndValueDate(party, today);
            case "T1" -> positionRepository.findByPartyAndValueDate(party, today.plusDays(1));
            case "T2" -> positionRepository.findByPartyAndValueDate(party, today.plusDays(2));
            case "FORWARD" -> positionRepository.findByPartyAndValueDateGreaterThan(party, today.plusDays(2));
            default -> throw new IllegalArgumentException("Invalid bucket. Use T0, T1, T2, or FORWARD");
        };
    }

    public List<Position> getPositionsByPartyAndDateRange(String party, LocalDate from, LocalDate to) {
        return positionRepository.findByPartyAndValueDateBetween(party, from, to);
    }
}
    

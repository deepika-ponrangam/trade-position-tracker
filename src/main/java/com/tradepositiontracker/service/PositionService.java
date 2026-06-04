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
    private final ExchangeRateService exchangeRateService;

    public void updatePositionsForNewTrade(Trade trade) {
        String tradingParty = trade.getTradingParty();
        String counterParty = trade.getCounterParty();
        String primaryCurrency = trade.getPrimaryCurrency();
        String secondaryCurrency = trade.getSecondaryCurrency();
        BigDecimal primaryAmount = trade.getPrimaryAmount();
        BigDecimal secondaryAmount = trade.getSecondaryAmount();
        LocalDate valueDate = trade.getValueDate();

        if (trade.getDirection() == Direction.BUY) {
            addExposure(tradingParty, primaryCurrency, valueDate, primaryAmount, trade.getTradeReference());
            addObligation(tradingParty, secondaryCurrency, valueDate, secondaryAmount, trade.getTradeReference());
            addExposure(counterParty, secondaryCurrency, valueDate, secondaryAmount, trade.getTradeReference());
            addObligation(counterParty, primaryCurrency, valueDate, primaryAmount, trade.getTradeReference());
        } else {
            addExposure(tradingParty, primaryCurrency, valueDate, primaryAmount, trade.getTradeReference());
            addObligation(tradingParty, secondaryCurrency, valueDate, secondaryAmount, trade.getTradeReference());
            addExposure(counterParty, secondaryCurrency, valueDate, secondaryAmount, trade.getTradeReference());
            addObligation(counterParty, primaryCurrency, valueDate, primaryAmount, trade.getTradeReference());
        }
    }

    public void reversePositionsForTrade(Trade trade) {
        String tradingParty = trade.getTradingParty();
        String counterParty = trade.getCounterParty();
        String primaryCurrency = trade.getPrimaryCurrency();
        String secondaryCurrency = trade.getSecondaryCurrency();
        BigDecimal primaryAmount = trade.getPrimaryAmount();
        BigDecimal secondaryAmount = trade.getSecondaryAmount();
        LocalDate valueDate = trade.getValueDate();

        if (trade.getDirection() == Direction.BUY) {
            reduceExposure(tradingParty, primaryCurrency, valueDate, primaryAmount, trade.getTradeReference());
            reduceObligation(tradingParty, secondaryCurrency, valueDate, secondaryAmount, trade.getTradeReference());
            reduceExposure(counterParty, secondaryCurrency, valueDate, secondaryAmount, trade.getTradeReference());
            reduceObligation(counterParty, primaryCurrency, valueDate, primaryAmount, trade.getTradeReference());
        } else {
            reduceExposure(tradingParty, secondaryCurrency, valueDate, secondaryAmount, trade.getTradeReference());
            reduceObligation(tradingParty, primaryCurrency, valueDate, primaryAmount, trade.getTradeReference());
            reduceExposure(counterParty, primaryCurrency, valueDate, primaryAmount, trade.getTradeReference());
            reduceObligation(counterParty, secondaryCurrency, valueDate, secondaryAmount, trade.getTradeReference());
        }
    }

    public void settlePositionsForTrade(Trade trade) {
        String tradingParty = trade.getTradingParty();
        String counterParty = trade.getCounterParty();
        String primaryCurrency = trade.getPrimaryCurrency();
        String secondaryCurrency = trade.getSecondaryCurrency();
        BigDecimal primaryAmount = trade.getPrimaryAmount();
        BigDecimal secondaryAmount = trade.getSecondaryAmount();
        LocalDate valueDate = trade.getValueDate();

        if (trade.getDirection() == Direction.BUY) {
            settlePosition(tradingParty, primaryCurrency, valueDate, primaryAmount, true, trade.getTradeReference());
            settlePosition(tradingParty, secondaryCurrency, valueDate, secondaryAmount, false, trade.getTradeReference());
            settlePosition(counterParty, primaryCurrency, valueDate, primaryAmount, false, trade.getTradeReference());
            settlePosition(counterParty, secondaryCurrency, valueDate, secondaryAmount, true, trade.getTradeReference());
        } else {
            settlePosition(tradingParty, primaryCurrency, valueDate, primaryAmount, false, trade.getTradeReference());
            settlePosition(tradingParty, secondaryCurrency, valueDate, secondaryAmount, true, trade.getTradeReference());
            settlePosition(counterParty, primaryCurrency, valueDate, primaryAmount, true, trade.getTradeReference());
            settlePosition(counterParty, secondaryCurrency, valueDate, secondaryAmount, false, trade.getTradeReference());
        }
    }

    private void addExposure(String party, String currency, LocalDate valueDate,
                              BigDecimal amount, String tradeReference) {
        Position position = getOrCreatePosition(party, currency, valueDate);
        BigDecimal prevExposure = position.getExposure();
        BigDecimal prevObligation = position.getObligation();
        BigDecimal prevNet = position.getNetPosition();

        position.setExposure(prevExposure.add(amount));
        updateUsdEquivalent(position);
        positionRepository.save(position);

        positionHistoryService.recordChange(position, tradeReference, PositionAction.TRADE_BOOKED,
                prevExposure, prevObligation, prevNet);
    }

    private void addObligation(String party, String currency, LocalDate valueDate,
                                BigDecimal amount, String tradeReference) {
        Position position = getOrCreatePosition(party, currency, valueDate);
        BigDecimal prevExposure = position.getExposure();
        BigDecimal prevObligation = position.getObligation();
        BigDecimal prevNet = position.getNetPosition();

        position.setObligation(prevObligation.add(amount));
        updateUsdEquivalent(position);
        positionRepository.save(position);

        positionHistoryService.recordChange(position, tradeReference, PositionAction.TRADE_BOOKED,
                prevExposure, prevObligation, prevNet);
    }

    private void reduceExposure(String party, String currency, LocalDate valueDate,
                                 BigDecimal amount, String tradeReference) {
        Position position = getOrCreatePosition(party, currency, valueDate);
        BigDecimal prevExposure = position.getExposure();
        BigDecimal prevObligation = position.getObligation();
        BigDecimal prevNet = position.getNetPosition();

        position.setExposure(prevExposure.subtract(amount));
        updateUsdEquivalent(position);
        positionRepository.save(position);

        positionHistoryService.recordChange(position, tradeReference, PositionAction.TRADE_REVERSED,
                prevExposure, prevObligation, prevNet);
    }

    private void reduceObligation(String party, String currency, LocalDate valueDate,
                                   BigDecimal amount, String tradeReference) {
        Position position = getOrCreatePosition(party, currency, valueDate);
        BigDecimal prevExposure = position.getExposure();
        BigDecimal prevObligation = position.getObligation();
        BigDecimal prevNet = position.getNetPosition();

        position.setObligation(prevObligation.subtract(amount));
        updateUsdEquivalent(position);
        positionRepository.save(position);

        positionHistoryService.recordChange(position, tradeReference, PositionAction.TRADE_REVERSED,
                prevExposure, prevObligation, prevNet);
    }

    private void settlePosition(String party, String currency, LocalDate valueDate,
                                 BigDecimal amount, boolean isReceiving, String tradeReference) {
        Position position = getOrCreatePosition(party, currency, valueDate);
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
        updateUsdEquivalent(position);
        positionRepository.save(position);

        positionHistoryService.recordChange(position, tradeReference, PositionAction.TRADE_SETTLED,
                prevExposure, prevObligation, prevNet);
    }

    private void updateUsdEquivalent(Position position) {
        if (position.getNetPosition().equals(BigDecimal.ZERO)) {
            position.setUsdEquivalent(BigDecimal.ZERO);
            return;
        }
        BigDecimal usdEquiv = exchangeRateService.getUsdEquivalent(
                position.getCurrency(), position.getNetPosition());
        if (usdEquiv != null) {
            position.setUsdEquivalent(usdEquiv);
        }
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

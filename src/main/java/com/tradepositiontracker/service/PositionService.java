package com.tradepositiontracker.service;

import com.tradepositiontracker.enums.Direction;
import com.tradepositiontracker.dto.PositionResponse;
import com.tradepositiontracker.model.Position;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.repository.PositionRepository;
import com.tradepositiontracker.util.CurrencyFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;
    private final ExchangeRateService exchangeRateService; 

    public Page<PositionResponse> getAllPositions(Pageable pageable) {
        return positionRepository.findAll(pageable).map(this::mapToResponse);
    }

    public List<PositionResponse> getPosition(String party, String currency) {
        return positionRepository.findByPartyAndCurrency(party, currency).stream()
                .map(this::mapToResponse).toList();
    }

    public List<PositionResponse> getPositionsByParty(String party) {
        return positionRepository.findByParty(party).stream()
                .map(this::mapToResponse).toList();
    }

    public List<PositionResponse> getPositionsByPartyAndCurrency(String party, String currency) {
        return positionRepository.findByPartyAndCurrency(party, currency).stream()
                .map(this::mapToResponse).toList();
    }

    public List<PositionResponse> getPositionsByPartyAndBucket(String party, String bucket) {
        LocalDate today = LocalDate.now();
        List<Position> positions = switch (bucket.toUpperCase()) {
            case "T0" -> positionRepository.findByPartyAndValueDate(party, today);
            case "T1" -> positionRepository.findByPartyAndValueDate(party, today.plusDays(1));
            case "T2" -> positionRepository.findByPartyAndValueDate(party, today.plusDays(2));
            case "FORWARD" -> positionRepository.findByPartyAndValueDateGreaterThan(party, today.plusDays(2));
            default -> throw new IllegalArgumentException("Invalid bucket. Use T0, T1, T2, or FORWARD");
        };
        return positions.stream().map(this::mapToResponse).toList();
    }

    public List<PositionResponse> getPositionsByPartyAndDateRange(String party, LocalDate from, LocalDate to) {
        return positionRepository.findByPartyAndValueDateBetween(party, from, to).stream()
                .map(this::mapToResponse).toList();
    }

    public void updatePositionsForNewTrade(Trade trade) {
        BigDecimal primaryAmount = trade.getPrimaryAmount();
        BigDecimal secondaryAmount = trade.getSecondaryAmount();
        LocalDate valueDate = trade.getValueDate();

        Position tradingPartyPrimaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getPrimaryCurrency(), valueDate);
        Position tradingPartySecondaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getSecondaryCurrency(), valueDate);
        Position counterPartyPrimaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getPrimaryCurrency(), valueDate);
        Position counterPartySecondaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getSecondaryCurrency(), valueDate);

        if (trade.getDirection() == Direction.BUY) {
            addExposure(tradingPartyPrimaryPosition, primaryAmount);
            addObligation(tradingPartySecondaryPosition, secondaryAmount);
            addExposure(counterPartySecondaryPosition, secondaryAmount);
            addObligation(counterPartyPrimaryPosition, primaryAmount);
        } else {
            addObligation(tradingPartyPrimaryPosition, primaryAmount);
            addExposure(tradingPartySecondaryPosition, secondaryAmount);
            addExposure(counterPartyPrimaryPosition, primaryAmount);
            addObligation(counterPartySecondaryPosition, secondaryAmount);
        }
    }

    public void reversePositionsForTrade(Trade trade) {
        BigDecimal primaryAmount = trade.getPrimaryAmount();
        BigDecimal secondaryAmount = trade.getSecondaryAmount();

        LocalDate valueDate = trade.getValueDate();

        Position tradingPartyPrimaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getPrimaryCurrency(), valueDate);
        Position tradingPartySecondaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getSecondaryCurrency(), valueDate);
        Position counterPartyPrimaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getPrimaryCurrency(), valueDate);
        Position counterPartySecondaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getSecondaryCurrency(), valueDate);

        if (trade.getDirection() == Direction.BUY) {
            reduceExposure(tradingPartyPrimaryPosition, primaryAmount);
            reduceObligation(tradingPartySecondaryPosition, secondaryAmount);
            reduceExposure(counterPartySecondaryPosition, secondaryAmount);
            reduceObligation(counterPartyPrimaryPosition, primaryAmount);
        } else {
            reduceExposure(tradingPartySecondaryPosition, secondaryAmount);
            reduceObligation(tradingPartyPrimaryPosition, primaryAmount);
            reduceExposure(counterPartyPrimaryPosition, primaryAmount);
            reduceObligation(counterPartySecondaryPosition, secondaryAmount);
        }
    }

    public void settlePositionsForTrade(Trade trade) {
        BigDecimal primaryAmount = trade.getPrimaryAmount();
        BigDecimal secondaryAmount = trade.getSecondaryAmount();
        LocalDate valueDate = trade.getValueDate();

        Position tradingPartyPrimaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getPrimaryCurrency(), valueDate);
        Position tradingPartySecondaryPosition = getOrCreatePosition(trade.getTradingParty(), trade.getSecondaryCurrency(), valueDate);
        Position counterPartyPrimaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getPrimaryCurrency(), valueDate);
        Position counterPartySecondaryPosition = getOrCreatePosition(trade.getCounterParty(), trade.getSecondaryCurrency(), valueDate);

        if (trade.getDirection() == Direction.BUY) {
            settlePosition(tradingPartyPrimaryPosition, primaryAmount, true);
            settlePosition(tradingPartySecondaryPosition, secondaryAmount, false);
            settlePosition(counterPartyPrimaryPosition, primaryAmount, false);
            settlePosition(counterPartySecondaryPosition, secondaryAmount, true);
        } else {
            settlePosition(tradingPartyPrimaryPosition, primaryAmount, false);
            settlePosition(tradingPartySecondaryPosition, secondaryAmount, true);
            settlePosition(counterPartyPrimaryPosition, primaryAmount, true);
            settlePosition(counterPartySecondaryPosition, secondaryAmount, false);
        }
    }

    private void recalculatePositionMetrics(Position position) {
        position.setNetPosition(position.getExposure().subtract(position.getObligation()));
        BigDecimal usdEq = exchangeRateService.getUsdEquivalent(position.getCurrency(), position.getNetPosition());
        if (usdEq != null) {
            position.setUsdEquivalent(usdEq);
        }
        positionRepository.save(position);
    }

    private void addExposure(Position position, BigDecimal amount) {
        position.setExposure(position.getExposure().add(amount));
        recalculatePositionMetrics(position);
    }

    private void addObligation(Position position, BigDecimal amount) {
        position.setObligation(position.getObligation().add(amount));
        recalculatePositionMetrics(position);
    }

    private void reduceExposure(Position position, BigDecimal amount) {
        position.setExposure(position.getExposure().subtract(amount));
        recalculatePositionMetrics(position);
    }

    private void reduceObligation(Position position, BigDecimal amount) {
        position.setObligation(position.getObligation().subtract(amount));
        recalculatePositionMetrics(position);
    }

    private void settlePosition(Position position, BigDecimal amount, boolean isReceiving) {
        if (isReceiving) {
            position.setExposure(position.getExposure().subtract(amount));
        } else {
            position.setObligation(position.getObligation().subtract(amount));
        }
        recalculatePositionMetrics(position);
    }

    private Position getOrCreatePosition(String party, String currency, LocalDate valueDate) {
        return positionRepository.findByPartyAndCurrencyAndValueDate(party, currency, valueDate)
                .orElse(new Position(party, currency, valueDate));
    }
    private PositionResponse mapToResponse(Position position) {
        return PositionResponse.fromEntity(position, position.getUsdEquivalent());
    }
}
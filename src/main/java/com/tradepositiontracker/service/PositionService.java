package com.tradepositiontracker.service;

import com.tradepositiontracker.model.Position;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.repository.PositionRepository;
import com.tradepositiontracker.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PositionService {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private TradeValidationService tradeValidationService;

    @Autowired
    private ProfitAndLossService profitAndLossService;

    @Transactional
    public Position processTrade(Trade trade) {
        tradeValidationService.validate(trade);
        trade.setTimestamp(LocalDateTime.now());
        tradeRepository.save(trade);

        Position position = positionRepository
                .findByTradingPartyAndInstrument(trade.getTradingParty(), trade.getInstrument())
                .orElse(new Position(trade.getTradingParty(), trade.getInstrument()));

        int tradeQuantity = trade.getSide().equals("BUY") ? trade.getQuantity() : -trade.getQuantity();

        if (position.getQuantity() == 0) {
            position.setQuantity(tradeQuantity);
            position.setAverageCostPrice(trade.getPrice());
        } else if (sameDirection(position.getQuantity(), tradeQuantity)) {
            double newAvgCost = calculateWeightedAverageCost(
                    Math.abs(position.getQuantity()), position.getAverageCostPrice(),
                    Math.abs(tradeQuantity), trade.getPrice());
            position.setQuantity(position.getQuantity() + tradeQuantity);
            position.setAverageCostPrice(newAvgCost);
        } else {
            handleOppositeDirectionTrade(position, trade, tradeQuantity);
        }

        return positionRepository.save(position);
    }

    private void handleOppositeDirectionTrade(Position position, Trade trade, int tradeQuantity) {
        int absExisting = Math.abs(position.getQuantity());
        int absTrade = Math.abs(tradeQuantity);

        double realizedPnl = profitAndLossService.calculateRealizedProfitAndLoss(position, trade);
        position.setRealizedProfitAndLoss(position.getRealizedProfitAndLoss() + realizedPnl);
        position.setQuantity(position.getQuantity() + tradeQuantity);

        if (position.getQuantity() == 0) {
            position.setAverageCostPrice(0);
        } else if (absTrade > absExisting) {
            position.setAverageCostPrice(trade.getPrice());
        }
    }

    private double calculateWeightedAverageCost(int existingQuantity, double existingAvgCost,
                                                 int newQuantity, double newPrice) {
        long totalCost = (long) (existingQuantity * existingAvgCost + newQuantity * newPrice);
        int totalQuantity = existingQuantity + newQuantity;
        return totalCost / totalQuantity;
    }

    private boolean sameDirection(int existingQuantity, int tradeQuantity) {
        return (existingQuantity > 0 && tradeQuantity > 0) || (existingQuantity < 0 && tradeQuantity < 0);
    }

    public List<Position> getPositionsByTradingParty(String tradingParty) {
        return positionRepository.findByTradingParty(tradingParty);
    }

    public Position getPosition(String tradingParty, String instrument) {
        return positionRepository.findByTradingPartyAndInstrument(tradingParty, instrument)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Position not found for " + tradingParty + " / " + instrument));
    }

    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }
}

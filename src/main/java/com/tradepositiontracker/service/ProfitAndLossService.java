package com.tradepositiontracker.service;

import com.tradepositiontracker.model.MarketPrice;
import com.tradepositiontracker.model.Position;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.repository.MarketPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfitAndLossService {

    @Autowired
    private MarketPriceRepository marketPriceRepository;

    public double calculateRealizedProfitAndLoss(Position position, Trade trade) {
        int closingQuantity = trade.getQuantity();

        double priceDifference;
        if (position.getQuantity() > 0) {
            priceDifference = trade.getPrice() - position.getAverageCostPrice();
        } else {
            priceDifference = position.getAverageCostPrice() - trade.getPrice();
        }

        return closingQuantity * priceDifference;
    }

    public double calculateUnrealizedProfitAndLoss(Position position) {
        if (position.getQuantity() == 0) {
            return 0.0;
        }

        MarketPrice marketPrice = marketPriceRepository
                .findByInstrument(position.getInstrument())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Market price not found for " + position.getInstrument()));

        double priceDifference;
        if (position.getQuantity() > 0) {
            priceDifference = marketPrice.getPrice() - position.getAverageCostPrice();
        } else {
            priceDifference = position.getAverageCostPrice() - marketPrice.getPrice();
        }

        return Math.abs(position.getQuantity()) * priceDifference;
    }
}

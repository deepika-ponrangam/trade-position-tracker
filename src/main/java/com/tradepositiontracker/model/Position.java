package com.tradepositiontracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "positions", uniqueConstraints = @UniqueConstraint(columnNames = {"tradingParty", "instrument"}))
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tradingParty;
    private String instrument;
    private int quantity;
    private double averageCostPrice;
    private double realizedProfitAndLoss;

    public Position() {
    }

    public Position(String tradingParty, String instrument) {
        this.tradingParty = tradingParty;
        this.instrument = instrument;
        this.quantity = 0;
        this.averageCostPrice = 0.0;
        this.realizedProfitAndLoss = 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTradingParty() {
        return tradingParty;
    }

    public void setTradingParty(String tradingParty) {
        this.tradingParty = tradingParty;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAverageCostPrice() {
        return averageCostPrice;
    }

    public void setAverageCostPrice(double averageCostPrice) {
        this.averageCostPrice = averageCostPrice;
    }

    public double getRealizedProfitAndLoss() {
        return realizedProfitAndLoss;
    }

    public void setRealizedProfitAndLoss(double realizedProfitAndLoss) {
        this.realizedProfitAndLoss = realizedProfitAndLoss;
    }
}

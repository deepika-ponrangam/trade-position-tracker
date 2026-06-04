package com.tradepositiontracker.model;

import com.tradepositiontracker.enums.Direction;
import com.tradepositiontracker.enums.TradeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trades")
public class Trade extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tradeReference;

    @Column(nullable = false)
    private String tradingParty;

    @Column(nullable = false)
    private String counterParty;

    @Column(nullable = false)
    private String primaryCurrency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal primaryAmount;

    @Column(nullable = false)
    private String secondaryCurrency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal secondaryAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(nullable = false)
    private LocalDate valueDate;

    @Column(nullable = false)
    private LocalDate tradeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    private LocalDateTime settledAt;
}

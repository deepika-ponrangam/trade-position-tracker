package com.tradepositiontracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "positions", uniqueConstraints = @UniqueConstraint(columnNames = {"party", "currency", "value_date"}))
public class Position extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String party;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private LocalDate valueDate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal exposure = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal obligation = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal netPosition = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal usdEquivalent = BigDecimal.ZERO;

    public Position(String party, String currency, LocalDate valueDate) {
        this.party = party;
        this.currency = currency;
        this.valueDate = valueDate;
        this.exposure = BigDecimal.ZERO;
        this.obligation = BigDecimal.ZERO;
        this.netPosition = BigDecimal.ZERO;
        this.usdEquivalent = BigDecimal.ZERO;
    }
}

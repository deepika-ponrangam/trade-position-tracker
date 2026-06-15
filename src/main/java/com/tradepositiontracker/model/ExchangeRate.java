package com.tradepositiontracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exchange_rates")
@Audited(withModifiedFlag = true)

public class ExchangeRate{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal rateToUsd;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ExchangeRate(String currency, BigDecimal rateToUsd) {
        this.currency = currency;
        this.rateToUsd = rateToUsd;
        this.updatedAt = LocalDateTime.now();
    }
}

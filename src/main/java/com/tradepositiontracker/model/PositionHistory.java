package com.tradepositiontracker.model;

import com.tradepositiontracker.enums.PositionAction;
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
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "position_history")
public class PositionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String party;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private LocalDate valueDate;

    @Column(nullable = false)
    private String tradeReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PositionAction action;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal previousExposure;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal updatedExposure;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal previousObligation;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal updatedObligation;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal previousNetPosition;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal updatedNetPosition;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}

package com.tradepositiontracker.model;
import com.tradepositiontracker.enums.TradeAction;
import com.tradepositiontracker.enums.TradeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_history")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TradeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String tradeReference;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeAction action;
    @Enumerated(EnumType.STRING)
    private TradeStatus previousStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus updatedStatus;
    private BigDecimal previousPrimaryAmount;
    private BigDecimal updatedPrimaryAmount;
    private BigDecimal previousSecondaryAmount;
    private BigDecimal updatedSecondaryAmount;
    @CreatedBy
    @Column(updatable = false)
    private String updatedBy;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime timestamp;
}

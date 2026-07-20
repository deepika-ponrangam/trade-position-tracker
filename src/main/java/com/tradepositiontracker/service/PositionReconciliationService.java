package com.tradepositiontracker.service;

import com.tradepositiontracker.enums.Direction;
import com.tradepositiontracker.enums.PositionAction;
import com.tradepositiontracker.enums.TradeStatus;
import com.tradepositiontracker.model.Position;
import com.tradepositiontracker.model.Trade;
import com.tradepositiontracker.repository.PositionRepository;
import com.tradepositiontracker.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
@Slf4j
public class PositionReconciliationService {

    private static final List<TradeStatus> OPEN_STATUSES = List.of(TradeStatus.PENDING, TradeStatus.MATCHED);
    private static final String RECONCILIATION_REFERENCE = "EOD_RECONCILIATION";

    private final TradeRepository tradeRepository;
    private final PositionRepository positionRepository;
    private final PositionHistoryService positionHistoryService;

    @Transactional
    public ReconciliationSummary reconcilePositions() {
        List<Trade> openTrades = tradeRepository.findByStatusIn(OPEN_STATUSES);
        log.info("EOD reconciliation: found {} open trades (PENDING/MATCHED)", openTrades.size());

        Map<PositionKey, BigDecimal[]> computed = new HashMap<>();
        for (Trade trade : openTrades) {
            applyTradeContribution(computed, trade);
        }

        int checked = 0;
        int corrected = 0;

        for (Map.Entry<PositionKey, BigDecimal[]> entry : computed.entrySet()) {
            PositionKey key = entry.getKey();
            BigDecimal expectedExposure = entry.getValue()[0];
            BigDecimal expectedObligation = entry.getValue()[1];

            Position position = positionRepository
                    .findByPartyAndCurrencyAndValueDate(key.party(), key.currency(), key.valueDate())
                    .orElseGet(() -> new Position(key.party(), key.currency(), key.valueDate()));

            boolean exposureDrift = position.getExposure().compareTo(expectedExposure) != 0;
            boolean obligationDrift = position.getObligation().compareTo(expectedObligation) != 0;

            checked++;

            if (exposureDrift || obligationDrift) {
                BigDecimal prevExposure = position.getExposure();
                BigDecimal prevObligation = position.getObligation();
                BigDecimal prevNet = position.getNetPosition();

                position.setExposure(expectedExposure);
                position.setObligation(expectedObligation);
                positionRepository.save(position);

                positionHistoryService.recordChange(position, RECONCILIATION_REFERENCE,
                        PositionAction.POSITION_RECONCILED, prevExposure, prevObligation, prevNet);

                corrected++;
                log.warn("Drift corrected for party={} currency={} valueDate={}: exposure {} -> {}, obligation {} -> {}",
                        key.party(), key.currency(), key.valueDate(),
                        prevExposure, expectedExposure, prevObligation, expectedObligation);
            }
        }

        ReconciliationSummary summary = new ReconciliationSummary(openTrades.size(), checked, corrected);
        log.info("EOD reconciliation complete: {}", summary);
        return summary;
    }

    private void applyTradeContribution(Map<PositionKey, BigDecimal[]> computed, Trade trade) {
        String tp = trade.getTradingParty();
        String cp = trade.getCounterParty();
        String primaryCcy = trade.getPrimaryCurrency();
        String secondaryCcy = trade.getSecondaryCurrency();
        LocalDate valueDate = trade.getValueDate();
        BigDecimal primaryAmt = trade.getPrimaryAmount();
        BigDecimal secondaryAmt = trade.getSecondaryAmount();

        if (trade.getDirection() == Direction.BUY) {
            addExposure(computed, tp, primaryCcy, valueDate, primaryAmt);
            addObligation(computed, tp, secondaryCcy, valueDate, secondaryAmt);
            addExposure(computed, cp, secondaryCcy, valueDate, secondaryAmt);
            addObligation(computed, cp, primaryCcy, valueDate, primaryAmt);
        } else {
            addExposure(computed, tp, secondaryCcy, valueDate, secondaryAmt);
            addObligation(computed, tp, primaryCcy, valueDate, primaryAmt);
            addExposure(computed, cp, primaryCcy, valueDate, primaryAmt);
            addObligation(computed, cp, secondaryCcy, valueDate, secondaryAmt);
        }
    }

    private void addExposure(Map<PositionKey, BigDecimal[]> map, String party, String currency,
                              LocalDate valueDate, BigDecimal amount) {
        slot(map, party, currency, valueDate)[0] = slot(map, party, currency, valueDate)[0].add(amount);
    }

    private void addObligation(Map<PositionKey, BigDecimal[]> map, String party, String currency,
                                LocalDate valueDate, BigDecimal amount) {
        slot(map, party, currency, valueDate)[1] = slot(map, party, currency, valueDate)[1].add(amount);
    }

    private BigDecimal[] slot(Map<PositionKey, BigDecimal[]> map, String party, String currency, LocalDate valueDate) {
        return map.computeIfAbsent(new PositionKey(party, currency, valueDate),
                k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
    }

    private record PositionKey(String party, String currency, LocalDate valueDate) {
    }

    public record ReconciliationSummary(int openTradesProcessed, int positionBucketsChecked, int driftsCorrected) {
    }
}
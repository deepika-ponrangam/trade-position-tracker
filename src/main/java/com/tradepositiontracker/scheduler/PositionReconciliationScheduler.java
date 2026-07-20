package com.tradepositiontracker.scheduler;

import com.tradepositiontracker.service.PositionReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class PositionReconciliationScheduler {

    private final PositionReconciliationService positionReconciliationService;

    @Scheduled(cron = "${scheduler.position-reconciliation.cron:0 15 0 * * *}",
               zone = "${scheduler.position-reconciliation.zone:UTC}")
    public void runEodReconciliation() {
        log.info("EOD position reconciliation job starting");
        try {
            var summary = positionReconciliationService.reconcilePositions();
            log.info("EOD position reconciliation job finished successfully: {}", summary);
        } catch (Exception e) {
            log.error("EOD position reconciliation job failed", e);
        }
    }
}
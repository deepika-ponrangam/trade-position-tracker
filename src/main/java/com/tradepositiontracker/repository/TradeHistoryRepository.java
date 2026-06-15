package com.tradepositiontracker.repository;
import com.tradepositiontracker.model.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {
    List<TradeHistory> findByTradeReferenceOrderByTimestampDesc(String tradeReference);
}

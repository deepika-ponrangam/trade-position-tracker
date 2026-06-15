package com.tradepositiontracker.service;
import com.tradepositiontracker.dto.TradeAuditResponse;
import com.tradepositiontracker.model.Trade;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import com.tradepositiontracker.dto.TradeResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import jakarta.persistence.Query;

@Service
@RequiredArgsConstructor
public class TradeAuditService {
    private static final String[] FIELD_NAMES = {
        "tradeReference", "tradingParty", "counterParty", "primaryCurrency",
        "primaryAmount", "secondaryCurrency", "secondaryAmount", "direction",
        "valueDate", "tradeDate", "status", "settledAt", "createdAt", "updatedAt"
    };
    private final EntityManager entityManager;
  
    public List<TradeAuditResponse> getTradeAuditHistory(String tradeReference){
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntityWithChanges(Trade.class, true)
                .add(AuditEntity.property("tradeReference").eq(tradeReference));

        List<Object[]> results = query.getResultList();
        if (results.isEmpty()) {
            return new ArrayList<>();
        }
        Long tradeId = ((Trade) results.get(0)[0]).getId();
        List<Integer> revs = new ArrayList<>();
        for (Object[] result : results) {
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) result[1];
            revs.add(revisionEntity.getId());
        }
            Map<Integer, Set<String>> changedFieldsByRev = getChangedFieldsBatch(tradeId, revs);
            List<TradeAuditResponse> auditResponses = new ArrayList<>();
            
            for (Object[] result : results){
                Trade trade = (Trade) result[0];
                DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) result[1];
                RevisionType revisionType = (RevisionType) result[2];
                Set<String> changedFields = changedFieldsByRev.getOrDefault(revisionEntity.getId(), Set.of() );
                LocalDateTime timestamp = LocalDateTime.ofInstant(revisionEntity.getRevisionDate().toInstant(), ZoneId.systemDefault());
                TradeResponse tradesnap = TradeResponse.fromEntity(trade);
                auditResponses.add(TradeAuditResponse.builder()
                    .changedAt(timestamp)
                    .revisionType(revisionType.name())
                    .changedFields(changedFields)
                    .tradeSnapshot(tradesnap)
                    .build());
            }
        return auditResponses;
    }
    private Map<Integer, Set<String>> getChangedFieldsBatch(Long id, List<Integer> revs) {
        if (revs.isEmpty()){
            return Map.of();
        }
        String sql = "SELECT rev, trade_reference_mod, trading_party_mod, counter_party_mod, primary_currency_mod, " +
                "primary_amount_mod, secondary_currency_mod, secondary_amount_mod, direction_mod, " +
                "value_date_mod, trade_date_mod, status_mod, settled_at_mod, created_at_mod, updated_at_mod " +
                "FROM trades_aud WHERE id = :id AND rev IN (:revs)";
        Query nativeQuery = entityManager.createNativeQuery(sql)
                .setParameter("id", id)
                .setParameter("revs", revs);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = nativeQuery.getResultList();

        Map<Integer, Set<String>> result = new HashMap<>();
        for (Object[] row : rows) {
                Integer rev = (Integer) row[0];
                Set<String> changed = new LinkedHashSet<>();
                for (int i = 0; i < FIELD_NAMES.length; i++) {
                    if (Boolean.TRUE.equals(row[i + 1])){
                    changed.add(FIELD_NAMES[i]);
                    } 
                }
                result.put(rev, changed);
        }
        return result;
    }
}

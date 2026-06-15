package com.tradepositiontracker.service;

import com.tradepositiontracker.dto.ExchangeRateAuditResponse;
import com.tradepositiontracker.dto.ExchangeRateResponse;
import com.tradepositiontracker.model.ExchangeRate;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;

import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExchangeRateAuditService {
    private static final String[] FIELD_NAMES = { "currency", "rateToUsd", "updatedAt" };
    private final EntityManager entityManager;

    public List<ExchangeRateAuditResponse> getExchangeRateAuditHistory(String currency) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        
        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntityWithChanges(ExchangeRate.class, true)
                .add(AuditEntity.property("currency").eq(currency.toUpperCase()));

        List<Object[]> results = query.getResultList();
        Long rateId = ((ExchangeRate) results.get(0)[0]).getId();

        List<Integer> revs = new ArrayList<>();

        for (Object[] result : results) {
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) result[1];
            revs.add(revisionEntity.getId());
        }
        Map<Integer, Set<String>> changedFieldsByRev = getChangedFieldsBatch(rateId, revs);
        List<ExchangeRateAuditResponse> auditResponses = new ArrayList<>();
        for (Object[] result : results) {
            ExchangeRate rate = (ExchangeRate) result[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) result[1];
            RevisionType revisionType = (RevisionType) result[2];
            Set<String> changedFields = changedFieldsByRev.getOrDefault(revisionEntity.getId(), Set.of());
            LocalDateTime timestamp = LocalDateTime.ofInstant(
                    revisionEntity.getRevisionDate().toInstant(), ZoneId.systemDefault());
            ExchangeRateResponse rateSnap = ExchangeRateResponse.fromEntity(rate);
            auditResponses.add(ExchangeRateAuditResponse.builder()
                    .changedAt(timestamp)
                    .revisionType(revisionType.name())
                    .changedFields(changedFields)
                    .rateSnapshot(rateSnap)
                    .build());
        }
        return auditResponses;
    }
    private Map<Integer, Set<String>> getChangedFieldsBatch(Long id, List<Integer> revs) {
        if (revs.isEmpty()) {
            return Map.of();
        }

        String sql = "SELECT rev, currency_mod, rate_to_usd_mod, updated_at_mod " +
                "FROM exchange_rates_aud WHERE id = :id AND rev IN (:revs)";

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
                if (Boolean.TRUE.equals(row[i + 1])) {   
                    changed.add(FIELD_NAMES[i]);
                }
            }
            result.put(rev, changed);
        }
        return result;
    }
}
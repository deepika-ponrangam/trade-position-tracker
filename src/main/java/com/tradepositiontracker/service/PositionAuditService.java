package com.tradepositiontracker.service;

import com.tradepositiontracker.dto.PositionAuditResponse;
import com.tradepositiontracker.dto.PositionResponse;
import com.tradepositiontracker.model.Position;
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
public class PositionAuditService{
    private final EntityManager entityManager;
    private static final String[] FIELD_NAMES = {
            "party", "currency", "valueDate", "exposure",
            "obligation", "netPosition", "usdEquivalent"
    };


    public List<PositionAuditResponse> getPositionAuditHistory(Long positionId){
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntityWithChanges(Position.class, true)
                .add(AuditEntity.id().eq(positionId));

        List<Object[]> results = query.getResultList();
        List<Integer> revs = new ArrayList<>();
        for (Object[] result : results) {
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) result[1];
            revs.add(revisionEntity.getId());
        }
        Map<Integer, Set<String>> changedFieldsByRev = getChangedFieldsBatch(positionId, revs);
        List<PositionAuditResponse> auditResponses = new ArrayList<>();

        for (Object[] result : results) {
            Position position = (Position) result[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) result[1];
            RevisionType revisionType = (RevisionType) result[2];
            Set<String> changedFields = changedFieldsByRev.getOrDefault(revisionEntity.getId(), Set.of() );
            LocalDateTime timestamp = LocalDateTime.ofInstant(revisionEntity.getRevisionDate().toInstant(), ZoneId.systemDefault());
            PositionResponse positionSnap = PositionResponse.fromEntity(position, position.getUsdEquivalent());
            auditResponses.add(PositionAuditResponse.builder()
                    .changedAt(timestamp)
                    .revisionType(revisionType.name())
                    .changedFields(changedFields)
                    .positionSnapshot(positionSnap)
                    .build());
        }
        return auditResponses;
    }
    private Map<Integer, Set<String>> getChangedFieldsBatch(Long id, List<Integer> revs) {
        if (revs.isEmpty()){
            return Map.of();
        }

        String sql = "SELECT rev, party_mod, currency_mod, value_date_mod, exposure_mod, " +
                "obligation_mod, net_position_mod, usd_equivalent_mod " +
                "FROM positions_aud WHERE id = :id AND rev IN(:revs)";

        Query nativeQuery = entityManager.createNativeQuery(sql)
                .setParameter("id", id)
                .setParameter("revs", revs);
        
        @SuppressWarnings("unchecked")
        List<Object[]> rows = nativeQuery.getResultList();

        Map<Integer, Set<String>> result = new HashMap<>();
        for (Object[] row : rows) {
            Integer rev = (Integer) row[0];
            Set<String> changed = new LinkedHashSet<>();
            for (int i = 0; i < FIELD_NAMES.length; i++){
                if (Boolean.TRUE.equals(row[i + 1])){
                    changed.add(FIELD_NAMES[i]);
                }
            }
            result.put(rev, changed);
        }
        return result;
    }
}
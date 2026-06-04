package com.tradepositiontracker.service;

import com.tradepositiontracker.enums.PositionAction;
import com.tradepositiontracker.model.Position;
import com.tradepositiontracker.model.PositionHistory;
import com.tradepositiontracker.repository.PositionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionHistoryService {

    private final PositionHistoryRepository positionHistoryRepository;

    public void recordChange(Position position, String tradeReference, PositionAction action,
                             BigDecimal previousExposure, BigDecimal previousObligation,
                             BigDecimal previousNetPosition) {
        PositionHistory history = new PositionHistory();
        history.setParty(position.getParty());
        history.setCurrency(position.getCurrency());
        history.setValueDate(position.getValueDate());
        history.setTradeReference(tradeReference);
        history.setAction(action);
        history.setPreviousExposure(previousExposure);
        history.setUpdatedExposure(position.getExposure());
        history.setPreviousObligation(previousObligation);
        history.setUpdatedObligation(position.getObligation());
        history.setPreviousNetPosition(previousNetPosition);
        history.setUpdatedNetPosition(position.getNetPosition());
        positionHistoryRepository.save(history);
    }

    public List<PositionHistory> getHistoryByParty(String party) {
        return positionHistoryRepository.findByPartyOrderByTimestampDesc(party);
    }

    public List<PositionHistory> getHistoryByPartyAndCurrency(String party, String currency) {
        return positionHistoryRepository.findByPartyAndCurrencyOrderByTimestampDesc(party, currency);
    }

    public List<PositionHistory> getHistoryByTradeReference(String tradeReference) {
        return positionHistoryRepository.findByTradeReferenceOrderByTimestampDesc(tradeReference);
    }
}

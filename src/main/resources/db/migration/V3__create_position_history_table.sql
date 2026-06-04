CREATE TABLE position_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    party VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    value_date DATE NOT NULL,
    trade_reference VARCHAR(255) NOT NULL,
    action VARCHAR(30) NOT NULL,
    previous_exposure DECIMAL(19, 4) NOT NULL,
    updated_exposure DECIMAL(19, 4) NOT NULL,
    previous_obligation DECIMAL(19, 4) NOT NULL,
    updated_obligation DECIMAL(19, 4) NOT NULL,
    previous_net_position DECIMAL(19, 4) NOT NULL,
    updated_net_position DECIMAL(19, 4) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_position_history_party ON position_history(party);
CREATE INDEX idx_position_history_party_currency ON position_history(party, currency);
CREATE INDEX idx_position_history_trade_ref ON position_history(trade_reference);

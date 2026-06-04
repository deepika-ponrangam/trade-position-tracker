CREATE TABLE positions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    party VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    value_date DATE NOT NULL,
    exposure DECIMAL(19, 4) NOT NULL DEFAULT 0,
    obligation DECIMAL(19, 4) NOT NULL DEFAULT 0,
    net_position DECIMAL(19, 4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_position_party_currency_date UNIQUE (party, currency, value_date)
);

CREATE INDEX idx_positions_party ON positions(party);
CREATE INDEX idx_positions_party_currency ON positions(party, currency);
CREATE INDEX idx_positions_party_value_date ON positions(party, value_date);

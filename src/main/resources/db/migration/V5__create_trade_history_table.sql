CREATE TABLE trade_history(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_reference VARCHAR(255) NOT NULL,
    action VARCHAR(30) NOT NULL,
    previous_status VARCHAR(50) NOT NULL,
    updated_status VARCHAR(50) NOT NULL,
    previous_primary_amount DECIMAL(19, 4),
    updated_primary_amount DECIMAL(19, 4),
    previous_secondary_amount DECIMAL(19, 4),
    updated_secondary_amount DECIMAL(19, 4),
    updated_by VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trade_history_reference ON trade_history(trade_reference);
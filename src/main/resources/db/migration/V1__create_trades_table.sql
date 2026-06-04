CREATE TABLE trades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_reference VARCHAR(255) NOT NULL UNIQUE,
    trading_party VARCHAR(255) NOT NULL,
    counter_party VARCHAR(255) NOT NULL,
    primary_currency VARCHAR(10) NOT NULL,
    primary_amount DECIMAL(19, 4) NOT NULL,
    secondary_currency VARCHAR(10) NOT NULL,
    secondary_amount DECIMAL(19, 4) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    value_date DATE NOT NULL,
    trade_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    settled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_trades_trade_reference ON trades(trade_reference);
CREATE INDEX idx_trades_trading_party ON trades(trading_party);
CREATE INDEX idx_trades_counter_party ON trades(counter_party);
CREATE INDEX idx_trades_status ON trades(status);
CREATE INDEX idx_trades_value_date ON trades(value_date);

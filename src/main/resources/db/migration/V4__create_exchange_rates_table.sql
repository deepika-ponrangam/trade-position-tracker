CREATE TABLE exchange_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    currency VARCHAR(10) NOT NULL UNIQUE,
    rate_to_usd DECIMAL(19, 4) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_exchange_rates_currency ON exchange_rates(currency);

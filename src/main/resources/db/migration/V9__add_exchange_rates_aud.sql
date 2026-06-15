CREATE TABLE exchange_rates_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype TINYINT,
    currency VARCHAR(10),
    rate_to_usd DECIMAL(19, 4),
    updated_at TIMESTAMP,
    PRIMARY KEY (id, rev)
);

ALTER TABLE exchange_rates_aud ADD COLUMN currency_mod BOOLEAN;
ALTER TABLE exchange_rates_aud ADD COLUMN rate_to_usd_mod BOOLEAN;
ALTER TABLE exchange_rates_aud ADD COLUMN updated_at_mod BOOLEAN;
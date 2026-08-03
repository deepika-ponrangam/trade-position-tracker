CREATE TABLE revinfo(
    rev INTEGER AUTO_INCREMENT PRIMARY KEY,
    revtstmp BIGINT
);
CREATE TABLE positions_aud(
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype TINYINT,
    party VARCHAR(255),
    currency VARCHAR(255),
    value_date DATE,
    exposure DECIMAL(19,4),
    obligation DECIMAL(19,4),
    net_position DECIMAL(19,4),
    usd_equivalent DECIMAL(19,4),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    PRIMARY KEY(id, rev)
);
CREATE TABLE trades_aud(
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype TINYINT,

    trade_reference VARCHAR(255),
    trading_party VARCHAR (255),
    counter_party VARCHAR(255),
    primary_currency VARCHAR(255),
    primary_amount DECIMAL(19, 4),
    secondary_currency VARCHAR(255),
    secondary_amount DECIMAL(19, 4),
    direction VARCHAR(255),
    trade_date DATE,
    value_date DATE,
    status VARCHAR(255),
    settled_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    PRIMARY KEY (id, rev)
);

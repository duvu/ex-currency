CREATE TABLE exchange_rate (
    id BIGSERIAL PRIMARY KEY,
    currency_code VARCHAR(3) NOT NULL,
    rate NUMERIC(19, 6) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_currency_code FOREIGN KEY (currency_code) REFERENCES currency(code)
);
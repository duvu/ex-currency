-- Add base column to currency table 
ALTER TABLE currency
ADD COLUMN base VARCHAR(3) NOT NULL DEFAULT 'USD';

-- Add base column to exchange_rate table
ALTER TABLE exchange_rate
ADD COLUMN base VARCHAR(3) NOT NULL DEFAULT 'USD';
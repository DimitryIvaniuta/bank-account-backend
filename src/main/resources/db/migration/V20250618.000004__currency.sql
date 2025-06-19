ALTER TABLE account
    RENAME COLUMN balance TO amount;

ALTER TABLE account
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'EUR';

UPDATE account
    SET currency = 'EUR'
    WHERE currency IS NULL;

ALTER TABLE operation
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'EUR';

UPDATE operation
SET currency = 'EUR'
WHERE currency IS NULL;

ALTER TABLE operation
    ADD COLUMN balance_currency VARCHAR(3) NOT NULL DEFAULT 'EUR';

UPDATE operation
SET balance_currency = 'EUR'
WHERE balance_currency IS NULL;
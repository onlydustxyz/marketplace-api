CREATE TYPE accounting.transaction_type AS ENUM ('DEPOSIT', 'SPEND');

ALTER TABLE accounting.sponsor_account_transactions
    ADD type accounting.transaction_type;

UPDATE accounting.sponsor_account_transactions
SET type = CASE WHEN amount >= 0 THEN 'DEPOSIT'::accounting.transaction_type ELSE 'SPEND'::accounting.transaction_type END
WHERE type IS NULL;

ALTER TABLE accounting.sponsor_account_transactions
    ALTER COLUMN type SET NOT NULL;

ALTER TABLE accounting.sponsor_account_transactions
    DROP COLUMN id;

ALTER TABLE accounting.sponsor_account_transactions
    ADD COLUMN id UUID DEFAULT gen_random_uuid();

ALTER TABLE accounting.sponsor_account_transactions
    ADD PRIMARY KEY (id);

ALTER TABLE accounting.sponsor_account_transactions
    ALTER COLUMN id DROP DEFAULT;

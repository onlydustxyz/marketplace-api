ALTER TABLE accounting.sponsor_account_transactions
    DROP COLUMN id;

ALTER TABLE accounting.sponsor_account_transactions
    ADD COLUMN id UUID;

ALTER TABLE accounting.sponsor_account_transactions
    ADD PRIMARY KEY (id);

DROP VIEW accounting.all_sponsor_account_transactions;

ALTER TABLE accounting.sponsor_account_transactions
    ALTER COLUMN network TYPE accounting.network USING network::text::accounting.network;

ALTER TABLE erc20
    ALTER COLUMN blockchain TYPE accounting.network USING blockchain::text::accounting.network;

DROP TYPE network;

ALTER TYPE accounting.network RENAME VALUE 'ethereum' TO 'ETHEREUM';
ALTER TYPE accounting.network RENAME VALUE 'aptos' TO 'APTOS';
ALTER TYPE accounting.network RENAME VALUE 'starknet' TO 'STARKNET';
ALTER TYPE accounting.network RENAME VALUE 'optimism' TO 'OPTIMISM';
ALTER TYPE accounting.network RENAME VALUE 'sepa' TO 'SEPA';

ALTER TYPE accounting.network ADD VALUE 'STELLAR';


CREATE VIEW accounting.all_sponsor_account_transactions AS
SELECT id         AS id,
       account_id AS sponsor_account_id,
       timestamp  AS timestamp,
       type       AS type,
       amount     AS amount,
       NULL       AS project_id,
       network    AS network
FROM accounting.sponsor_account_transactions
UNION
SELECT id         AS id,
       account_id AS sponsor_account_id,
       timestamp  AS timestamp,
       type       AS type,
       amount     AS amount,
       project_id AS project_id,
       NULL       AS network
FROM accounting.sponsor_account_allowance_transactions;
create table accounting.receipts
(
    id                         UUID PRIMARY KEY,
    created_at                 TIMESTAMP          NOT NULL,
    network                    accounting.network NOT NULL,
    third_party_name           TEXT               NOT NULL,
    third_party_account_number TEXT               NOT NULL,
    transaction_reference      TEXT               NOT NULL,
    tech_created_at            TIMESTAMP          NOT NULL DEFAULT now(),
    tech_updated_at            TIMESTAMP          NOT NULL DEFAULT now()
);

CREATE TRIGGER accounting_receipts_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.receipts
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE TABLE accounting.rewards_receipts
(
    reward_id  UUID NOT NULL REFERENCES rewards (id),
    receipt_id UUID NOT NULL REFERENCES accounting.receipts (id),
    PRIMARY KEY (reward_id, receipt_id)
);

INSERT INTO accounting.receipts (id, created_at, network, third_party_account_number, third_party_name, transaction_reference)
SELECT p.id,
       processed_at,
       'ethereum'::accounting.network,
       coalesce(p.receipt #>> '{Ethereum, recipient_address}', p.receipt #>> '{Ethereum, recipient_ens}'),
       coalesce(u.github_login, CAST(pr.recipient_id AS TEXT)),
       p.receipt #>> '{Ethereum, transaction_hash}'
FROM payments p
         JOIN payment_requests pr on pr.id = p.request_id
         LEFT JOIN iam.users u on u.github_user_id = pr.recipient_id
WHERE p.receipt -> 'Ethereum' IS NOT NULL;

INSERT INTO accounting.receipts (id, created_at, network, third_party_account_number, third_party_name, transaction_reference)
SELECT p.id,
       processed_at,
       'sepa'::accounting.network,
       p.receipt #>> '{Sepa, recipient_iban}',
       coalesce(u.github_login, CAST(pr.recipient_id AS TEXT)),
       p.receipt #>> '{Sepa, transaction_reference}'
FROM payments p
         JOIN payment_requests pr on pr.id = p.request_id
         LEFT JOIN iam.users u on u.github_user_id = pr.recipient_id
WHERE p.receipt -> 'Sepa' IS NOT NULL;

INSERT INTO accounting.rewards_receipts (reward_id, receipt_id)
SELECT request_id, id
FROM payments;

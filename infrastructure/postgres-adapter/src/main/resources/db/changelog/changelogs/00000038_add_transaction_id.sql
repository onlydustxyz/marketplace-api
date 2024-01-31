DROP TABLE accounting.ledger_transactions;

CREATE TABLE accounting.ledger_transactions
(
    id              UUID PRIMARY KEY,
    ledger_id       UUID      NOT NULL,
    network         network   NOT NULL,
    amount          NUMERIC   NOT NULL,
    locked_until    TIMESTAMP,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);
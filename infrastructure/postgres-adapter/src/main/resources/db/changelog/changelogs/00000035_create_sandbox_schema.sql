CREATE SCHEMA sandbox;

-- Account Book
CREATE TABLE sandbox.account_books
(
    id              UUID PRIMARY KEY,
    currency_id     UUID      NOT NULL UNIQUE REFERENCES currencies (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER account_books_events_set_tech_updated_at
    BEFORE UPDATE
    ON sandbox.account_books
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Account Book events

CREATE TABLE sandbox.account_books_events
(
    id              BIGSERIAL PRIMARY KEY,
    account_book_id UUID      NOT NULL REFERENCES sandbox.account_books (id),
    payload         JSONB     NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER account_books_events_set_tech_updated_at
    BEFORE UPDATE
    ON sandbox.account_books_events
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE INDEX account_books_events_tech_created_at_idx
    ON sandbox.account_books_events (account_book_id, tech_created_at);

-- Ledgers
CREATE TABLE sandbox.ledgers
(
    id              UUID PRIMARY KEY,
    currency_id     UUID      NOT NULL REFERENCES currencies (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER ledgers_set_tech_updated_at
    BEFORE UPDATE
    ON sandbox.ledgers
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Sponsor Ledgers
CREATE TABLE sandbox.sponsor_ledgers
(
    ledger_id       UUID      NOT NULL REFERENCES sandbox.ledgers (id),
    sponsor_id      UUID      NOT NULL REFERENCES sponsors (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ledger_id, sponsor_id)
);

CREATE TRIGGER sponsor_ledgers_set_tech_updated_at
    BEFORE UPDATE
    ON sandbox.sponsor_ledgers
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Project Ledgers
CREATE TABLE sandbox.project_ledgers
(
    ledger_id       UUID      NOT NULL REFERENCES sandbox.ledgers (id),
    project_id      UUID      NOT NULL REFERENCES projects (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ledger_id, project_id)
);

CREATE TRIGGER project_ledgers_set_tech_updated_at
    BEFORE UPDATE
    ON sandbox.project_ledgers
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Contributor Ledgers
CREATE TABLE sandbox.contributor_ledgers
(
    ledger_id       UUID      NOT NULL REFERENCES sandbox.ledgers (id),
    github_user_id  BIGINT    NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ledger_id, github_user_id)
);

CREATE TRIGGER contributor_ledgers_set_tech_updated_at
    BEFORE UPDATE
    ON sandbox.contributor_ledgers
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Ledger Transactions
CREATE TABLE sandbox.ledger_transactions
(
    id              BIGSERIAL PRIMARY KEY,
    ledger_id       UUID      NOT NULL,
    network         network   NOT NULL,
    amount          NUMERIC   NOT NULL,
    locked_until    TIMESTAMP,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER ledger_transactions_set_tech_updated_at
    BEFORE UPDATE
    ON sandbox.ledger_transactions
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

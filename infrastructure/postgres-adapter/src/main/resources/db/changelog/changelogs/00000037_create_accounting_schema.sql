CREATE SCHEMA accounting;

-- Account Book
CREATE TABLE accounting.account_books
(
    id              UUID PRIMARY KEY,
    currency_id     UUID      NOT NULL UNIQUE REFERENCES currencies (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER account_books_events_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.account_books
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Account Book events

CREATE TABLE accounting.account_books_events
(
    id              BIGSERIAL PRIMARY KEY,
    account_book_id UUID      NOT NULL REFERENCES accounting.account_books (id),
    payload         JSONB     NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER account_books_events_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.account_books_events
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE INDEX account_books_events_tech_created_at_idx
    ON accounting.account_books_events (account_book_id, tech_created_at);

-- Ledgers
CREATE TABLE accounting.ledgers
(
    id              UUID PRIMARY KEY,
    currency_id     UUID      NOT NULL REFERENCES currencies (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER ledgers_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.ledgers
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Sponsor Ledgers
CREATE TABLE accounting.sponsor_ledgers
(
    ledger_id       UUID      NOT NULL REFERENCES accounting.ledgers (id),
    sponsor_id      UUID      NOT NULL REFERENCES sponsors (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ledger_id, sponsor_id)
);

CREATE TRIGGER sponsor_ledgers_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.sponsor_ledgers
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Project Ledgers
CREATE TABLE accounting.project_ledgers
(
    ledger_id       UUID      NOT NULL REFERENCES accounting.ledgers (id),
    project_id      UUID      NOT NULL REFERENCES projects (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ledger_id, project_id)
);

CREATE TRIGGER project_ledgers_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.project_ledgers
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Contributor Ledgers
CREATE TABLE accounting.contributor_ledgers
(
    ledger_id       UUID      NOT NULL REFERENCES accounting.ledgers (id),
    github_user_id  BIGINT    NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ledger_id, github_user_id)
);

CREATE TRIGGER contributor_ledgers_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.contributor_ledgers
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Ledger Transactions
CREATE TABLE accounting.ledger_transactions
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
    ON accounting.ledger_transactions
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

DROP SCHEMA accounting CASCADE;
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

-- Sponsor accounts
CREATE TABLE accounting.sponsor_accounts
(
    id              UUID PRIMARY KEY,
    currency_id     UUID      NOT NULL REFERENCES currencies (id),
    sponsor_id      UUID      NOT NULL REFERENCES sponsors (id),
    locked_until    TIMESTAMP,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER sponsor_accounts_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.sponsor_accounts
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Sponsor Accounts Transactions
CREATE TABLE accounting.sponsor_account_transactions
(
    id                         BIGSERIAL PRIMARY KEY,
    account_id                 UUID      NOT NULL,
    network                    network   NOT NULL,
    reference                  TEXT      NOT NULL,
    amount                     NUMERIC   NOT NULL,
    third_party_name           TEXT      NOT NULL,
    third_party_account_number TEXT      NOT NULL,
    tech_created_at            TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at            TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER sponsor_account_transactions_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.sponsor_account_transactions
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

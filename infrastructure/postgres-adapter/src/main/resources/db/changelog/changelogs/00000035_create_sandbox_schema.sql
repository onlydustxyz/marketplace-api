CREATE SCHEMA sandbox;

-- Account Book
CREATE TABLE sandbox.account_books
(
    id              UUID PRIMARY KEY,
    currency_id     UUID      NOT NULL UNIQUE REFERENCES currencies (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER accounting_book_events_set_tech_updated_at
    BEFORE UPDATE
    ON sandbox.account_book_events
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Account Book events

CREATE TABLE sandbox.account_book_events
(
    id              BIGSERIAL PRIMARY KEY,
    account_book_id UUID      NOT NULL REFERENCES sandbox.account_books (id),
    type            TEXT      NOT NULL,
    payload         JSONB     NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER accounting_book_events_set_tech_updated_at
    BEFORE UPDATE
    ON sandbox.account_book_events
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE INDEX accounting_book_events_tech_created_at_idx
    ON sandbox.account_book_events (account_book_id, tech_created_at);

-- Sponsor Accounts
CREATE TABLE sponsor_accounts
(
    id              UUID PRIMARY KEY,
    currency_id     UUID      NOT NULL REFERENCES currencies (id),
    sponsor_id      UUID      NOT NULL REFERENCES sponsors (id),
    UNIQUE (currency_id, sponsor_id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER sponsor_accounts_set_tech_updated_at
    BEFORE UPDATE
    ON sponsor_accounts
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Project Accounts
CREATE TABLE project_accounts
(
    id              UUID PRIMARY KEY,
    currency_id     UUID      NOT NULL REFERENCES currencies (id),
    project_id      UUID      NOT NULL REFERENCES projects (id),
    UNIQUE (currency_id, project_id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER project_accounts_set_tech_updated_at
    BEFORE UPDATE
    ON project_accounts
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- Contributor Accounts
CREATE TABLE contributor_accounts
(
    id              UUID PRIMARY KEY,
    currency_id     UUID      NOT NULL REFERENCES currencies (id),
    github_user_id  BIGINT    NOT NULL references indexer_exp.github_accounts (id),
    UNIQUE (currency_id, github_user_id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER contributor_accounts_set_tech_updated_at
    BEFORE UPDATE
    ON contributor_accounts
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

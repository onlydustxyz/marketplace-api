DROP TABLE accounting.contributor_ledgers;

-- Contributor Ledgers
CREATE TABLE accounting.reward_ledgers
(
    ledger_id       UUID      NOT NULL REFERENCES accounting.ledgers (id),
    reward_id       UUID      NOT NULL REFERENCES public.payment_requests (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ledger_id, reward_id)
);

CREATE TRIGGER reward_ledgers_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.reward_ledgers
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();
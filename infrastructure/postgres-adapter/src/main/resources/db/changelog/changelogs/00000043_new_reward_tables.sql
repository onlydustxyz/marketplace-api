CREATE TABLE public.rewards
(
    id                  UUID PRIMARY KEY,
    project_id          UUID      NOT NULL REFERENCES projects (id),
    requestor_id        UUID      NOT NULL REFERENCES iam.users (id),
    recipient_id        BIGINT    NOT NULL,
    currency            currency  NOT NULL,
    amount              NUMERIC   NOT NULL,
    requested_at        TIMESTAMP NOT NULL,
    invoice_received_at TIMESTAMP,
    tech_created_at     TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER rewards_set_tech_updated_at
    BEFORE UPDATE
    ON public.rewards
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE TABLE public.reward_items
(
    reward_id       UUID              NOT NULL REFERENCES rewards (id),
    number          BIGINT            NOT NULL,
    repo_id         BIGINT            NOT NULL,
    id              TEXT              NOT NULL,
    type            contribution_type NOT NULL,
    project_id      UUID              NOT NULL REFERENCES projects (id),
    recipient_id    BIGINT            NOT NULL,
    tech_created_at TIMESTAMP         NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP         NOT NULL DEFAULT now(),
    PRIMARY KEY (reward_id, repo_id, number)
);

CREATE TRIGGER reward_items_set_tech_updated_at
    BEFORE UPDATE
    ON public.reward_items
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE TABLE accounting.reward_statuses
(
    reward_id               UUID PRIMARY KEY REFERENCES rewards (id),
    is_individual           BOOLEAN,
    kycb_verified           BOOLEAN   NOT NULL,
    us_recipient            BOOLEAN,
    reward_currency         currency  NOT NULL,
    current_year_usd_total  NUMERIC   NOT NULL,
    payout_info_filled      BOOLEAN   NOT NULL,
    sponsor_has_enough_fund BOOLEAN   NOT NULL,
    unlock_date             TIMESTAMP,
    payment_requested       BOOLEAN   NOT NULL,
    invoice_approved        BOOLEAN   NOT NULL,
    paid                    BOOLEAN   NOT NULL,
    tech_created_at         TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER accounting_reward_statuses_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.reward_statuses
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

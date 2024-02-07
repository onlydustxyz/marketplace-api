CREATE TABLE accounting.historical_quotes
(
    timestamp   TIMESTAMP NOT NULL,
    currency_id UUID      NOT NULL REFERENCES currencies (id),
    base_id     UUID      NOT NULL REFERENCES currencies (id),
    price       NUMERIC   NOT NULL,
    PRIMARY KEY (timestamp, currency_id, base_id)
);

CREATE INDEX historical_quotes_currency_idx ON accounting.historical_quotes (timestamp DESC, currency_id, base_id, price);

DROP TABLE public.quotes;

CREATE TABLE public.rewards
(
    id                    UUID PRIMARY KEY,
    project_id            UUID      NOT NULL REFERENCES projects (id),
    requestor_id          UUID      NOT NULL REFERENCES iam.users (id),
    recipient_id          BIGINT    NOT NULL,
    currency              currency  NOT NULL,
    amount                NUMERIC   NOT NULL,
    amount_usd_equivalent NUMERIC,
    requested_at          TIMESTAMP NOT NULL,
    invoice_received_at   TIMESTAMP,
    tech_created_at       TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at       TIMESTAMP NOT NULL DEFAULT now()
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
    sponsor_has_enough_fund BOOLEAN,
    unlock_date             TIMESTAMP,
    payment_requested_at    TIMESTAMP,
    paid_at                 TIMESTAMP,
    networks                network[],
    tech_created_at         TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER accounting_reward_statuses_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.reward_statuses
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();



WITH selected_billing_profile AS
         (SELECT ubpt.user_id,
                 ubpt.billing_profile_type = 'INDIVIDUAL' as is_individual,
                 CASE
                     WHEN ubpt.billing_profile_type = 'INDIVIDUAL' THEN ibp.verification_status = 'VERIFIED'
                     ELSE cbp.verification_status = 'VERIFIED'
                     END                                  as verified,
                 CASE
                     WHEN ubpt.billing_profile_type = 'INDIVIDUAL' THEN ibp.us_citizen
                     ELSE cbp.us_entity
                     END                                  as us_resident
          FROM user_billing_profile_types ubpt
                   LEFT JOIN individual_billing_profiles ibp on ibp.user_id = ubpt.user_id
                   LEFT JOIN company_billing_profiles cbp on cbp.user_id = ubpt.user_id),

     user_payout_networks AS
         (SELECT coalesce(w.user_id, ba.user_id)                                      AS user_id,
                 CASE WHEN w.user_id IS NOT NULL THEN array_agg(w.network) END ||
                 CASE WHEN ba.user_id IS NOT NULL THEN '{sepa, swift}'::network[] END AS networks
          FROM wallets w
                   FULL OUTER JOIN bank_accounts ba ON ba.user_id = w.user_id
          GROUP BY w.user_id, ba.user_id),

     yearly_usd_total_per_recipient AS
         (SELECT r.recipient_id,
                 SUM(r.amount_usd_equivalent) as yearly_usd_total
          FROM public.rewards r
                   JOIN accounting.reward_statuses rs ON r.id = rs.reward_id AND rs.payment_requested_at IS NOT NULL
          WHERE rs.payment_requested_at >= date_trunc('year', now())
            AND rs.payment_requested_at < date_trunc('year', now()) + interval '1 year'
          GROUP BY r.recipient_id),

     reward_statuses_data AS
         (SELECT reward_id,
                 bp.is_individual,
                 bp.verified                         as kycb_verified,
                 bp.us_resident                      as us_recipient,
                 r.currency                          as reward_currency,
                 coalesce(yutpr.yearly_usd_total, 0) as current_year_usd_total,
                 r.amount_usd_equivalent             as reward_usd_equivalent,
                 upn.networks @> rs.networks         as payout_info_filled,
                 rs.sponsor_has_enough_fund,
                 rs.unlock_date,
                 rs.payment_requested_at,
                 rs.paid_at
          FROM accounting.reward_statuses rs
                   JOIN rewards r on r.id = rs.reward_id
                   LEFT JOIN iam.users u on u.github_user_id = r.recipient_id
                   LEFT JOIN selected_billing_profile bp on bp.user_id = u.id
                   LEFT JOIN user_payout_networks upn on upn.user_id = u.id
                   LEFT JOIN yearly_usd_total_per_recipient yutpr on yutpr.recipient_id = r.recipient_id)

SELECT reward_id,
       CASE
           WHEN s.paid_at IS NOT NULL THEN 'COMPLETE'::reward_status
           WHEN s.is_individual IS NULL THEN 'PENDING_BILLING_PROFILE'::reward_status
           WHEN s.kycb_verified IS NOT TRUE THEN 'PENDING_VERIFICATION'::reward_status
           WHEN s.us_recipient IS TRUE AND s.reward_currency = 'strk'::currency THEN 'PAYMENT_BLOCKED'::reward_status
           WHEN s.is_individual IS TRUE AND s.current_year_usd_total + s.reward_usd_equivalent > 5000 THEN 'PAYMENT_BLOCKED'::reward_status
           WHEN s.payout_info_filled IS NOT TRUE THEN 'PAYOUT_INFO_MISSING'::reward_status
           WHEN s.unlock_date IS NOT NULL AND s.unlock_date > NOW() THEN 'LOCKED'::reward_status
           WHEN s.sponsor_has_enough_fund IS NOT TRUE THEN 'LOCKED'::reward_status
           WHEN s.payment_requested_at IS NULL THEN 'PENDING_REQUEST'::reward_status
           ELSE 'PROCESSING'::reward_status
           END
           as status
FROM reward_statuses_data s
;

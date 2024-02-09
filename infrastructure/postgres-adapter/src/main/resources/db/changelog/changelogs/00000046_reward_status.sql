ALTER TABLE accounting.reward_status_data
    RENAME COLUMN payment_requested_at TO invoice_received_at;
ALTER TABLE accounting.reward_status_data
    ADD COLUMN amount_usd_equivalent NUMERIC;

ALTER TABLE rewards
    DROP COLUMN invoice_received_at;
ALTER TABLE rewards
    DROP COLUMN amount_usd_equivalent;



CREATE VIEW accounting.reward_usd_equivalent_data AS
WITH selected_billing_profile AS
         (SELECT ubpt.user_id,
                 CASE
                     WHEN ubpt.billing_profile_type = 'INDIVIDUAL' THEN ibp.updated_at
                     ELSE cbp.updated_at
                     END as verified_at
          FROM user_billing_profile_types ubpt
                   LEFT JOIN individual_billing_profiles ibp ON ibp.user_id = ubpt.user_id AND ibp.verification_status = 'VERIFIED'
                   LEFT JOIN company_billing_profiles cbp ON cbp.user_id = ubpt.user_id AND cbp.verification_status = 'VERIFIED'),

     currency_quote_available_at AS
         (SELECT hq.currency_id, min(hq.timestamp) as available_at
          FROM accounting.historical_quotes hq
                   JOIN currencies usd ON usd.code = 'USD' AND usd.id = hq.base_id
          GROUP BY hq.currency_id)

SELECT r.id             as reward_id,
       r.requested_at   as reward_created_at,
       c.id             as reward_currency_id,
       bp.verified_at   as kycb_verified_at,
       cqa.available_at as currency_quote_available_at,
       rs.unlock_date   as unlock_date
FROM accounting.reward_status_data rs
         JOIN rewards r on r.id = rs.reward_id
         LEFT JOIN iam.users u on u.github_user_id = r.recipient_id
         LEFT JOIN selected_billing_profile bp on bp.user_id = u.id
         JOIN currencies c ON c.code = upper(r.currency::text)
         LEFT JOIN currency_quote_available_at cqa on cqa.currency_id = c.id;



CREATE VIEW accounting.reward_statuses AS
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
                 SUM(rs.amount_usd_equivalent) as yearly_usd_total
          FROM public.rewards r
                   JOIN accounting.reward_status_data rs ON r.id = rs.reward_id AND rs.payment_requested_at IS NOT NULL
          WHERE rs.payment_requested_at >= date_trunc('year', now())
            AND rs.payment_requested_at < date_trunc('year', now()) + interval '1 year'
          GROUP BY r.recipient_id),

     aggregated_reward_status_data AS
         (SELECT reward_id,
                 bp.is_individual,
                 bp.verified                         as kycb_verified,
                 bp.us_resident                      as us_recipient,
                 r.currency                          as reward_currency,
                 coalesce(yutpr.yearly_usd_total, 0) as current_year_usd_total,
                 rs.amount_usd_equivalent            as reward_usd_equivalent,
                 upn.networks @> rs.networks         as payout_info_filled,
                 rs.sponsor_has_enough_fund,
                 rs.unlock_date,
                 rs.invoice_received_at              as payment_requested_at,
                 rs.paid_at
          FROM accounting.reward_status_data rs
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
FROM aggregated_reward_status_data s
;
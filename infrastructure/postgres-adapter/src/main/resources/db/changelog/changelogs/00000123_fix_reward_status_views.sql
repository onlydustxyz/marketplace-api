CREATE OR REPLACE VIEW accounting.reward_statuses AS
WITH billing_profile_payout_networks AS
         (SELECT coalesce(w.billing_profile_id, ba.billing_profile_id)                               AS billing_profile_id,
                 CASE WHEN w.billing_profile_id IS NOT NULL THEN array_agg(w.network) END ||
                 CASE WHEN ba.billing_profile_id IS NOT NULL THEN '{sepa}'::accounting.network[] END AS networks
          FROM accounting.wallets w
                   FULL OUTER JOIN accounting.bank_accounts ba ON ba.billing_profile_id = w.billing_profile_id
          GROUP BY w.billing_profile_id, ba.billing_profile_id),

     yearly_usd_total_per_recipient AS
         (SELECT r.recipient_id,
                 SUM(rs.amount_usd_equivalent) as yearly_usd_total
          FROM public.rewards r
                   JOIN accounting.reward_status_data rs ON r.id = rs.reward_id AND rs.invoice_received_at IS NOT NULL
          WHERE rs.invoice_received_at >= date_trunc('year', now())
            AND rs.invoice_received_at < date_trunc('year', now()) + interval '1 year'
          GROUP BY r.recipient_id),

     aggregated_reward_status_data AS
         (SELECT reward_id,
                 u.id IS NOT NULL                        as is_registered,
                 bp.type = 'INDIVIDUAL'                  as is_individual,
                 bp.verification_status = 'VERIFIED'     as kycb_verified,
                 coalesce(kyc.us_citizen, kyb.us_entity) as us_recipient,
                 c.code                                  as reward_currency,
                 coalesce(yutpr.yearly_usd_total, 0)     as current_year_usd_total,
                 rs.amount_usd_equivalent                as reward_usd_equivalent,
                 bppn.networks @> rs.networks            as payout_info_filled,
                 rs.sponsor_has_enough_fund,
                 rs.unlock_date,
                 rs.invoice_received_at                  as payment_requested_at,
                 rs.paid_at
          FROM accounting.reward_status_data rs
                   JOIN rewards r on r.id = rs.reward_id
                   JOIN currencies c on c.id = r.currency_id
                   LEFT JOIN iam.users u on u.github_user_id = r.recipient_id
                   LEFT JOIN accounting.payout_preferences pp on pp.project_id = r.project_id and pp.user_id = u.id
                   LEFT JOIN accounting.billing_profiles bp ON bp.id = coalesce(r.billing_profile_id, pp.billing_profile_id)
                   LEFT JOIN accounting.kyc kyc on kyc.billing_profile_id = bp.id
                   LEFT JOIN accounting.kyb kyb on kyb.billing_profile_id = bp.id
                   LEFT JOIN billing_profile_payout_networks bppn on bppn.billing_profile_id = bp.id
                   LEFT JOIN yearly_usd_total_per_recipient yutpr on yutpr.recipient_id = r.recipient_id)

SELECT reward_id,
       CASE
           WHEN s.paid_at IS NOT NULL THEN 'COMPLETE'::accounting.reward_status
           WHEN NOT s.is_registered THEN 'PENDING_SIGNUP'::accounting.reward_status
           WHEN s.is_individual IS NULL THEN 'PENDING_BILLING_PROFILE'::accounting.reward_status
           WHEN s.kycb_verified IS NOT TRUE THEN 'PENDING_VERIFICATION'::accounting.reward_status
           WHEN s.us_recipient IS TRUE AND s.reward_currency = 'STRK' THEN 'PAYMENT_BLOCKED'::accounting.reward_status
           WHEN s.is_individual IS TRUE AND s.current_year_usd_total + s.reward_usd_equivalent > 5000 THEN 'PAYMENT_BLOCKED'::accounting.reward_status
           WHEN s.payout_info_filled IS NOT TRUE THEN 'PAYOUT_INFO_MISSING'::accounting.reward_status
           WHEN s.unlock_date IS NOT NULL AND s.unlock_date > NOW() THEN 'LOCKED'::accounting.reward_status
           WHEN s.sponsor_has_enough_fund IS NOT TRUE THEN 'LOCKED'::accounting.reward_status
           WHEN s.payment_requested_at IS NULL THEN 'PENDING_REQUEST'::accounting.reward_status
           ELSE 'PROCESSING'::accounting.reward_status
           END
           as status
FROM aggregated_reward_status_data s
;

CREATE OR REPLACE VIEW accounting.reward_usd_equivalent_data
            (reward_id, reward_created_at, reward_currency_id, kycb_verified_at, currency_quote_available_at,
             unlock_date, reward_amount)
AS
WITH currency_quote_available_at AS (SELECT hq.base_id,
                                            min(hq."timestamp") AS available_at
                                     FROM accounting.historical_quotes hq
                                              JOIN currencies usd ON usd.code = 'USD'::text AND usd.id = hq.target_id
                                     GROUP BY hq.base_id)
SELECT r.id               AS reward_id,
       r.requested_at     AS reward_created_at,
       r.currency_id      AS reward_currency_id,
       bp.tech_updated_at AS kycb_verified_at,
       cqa.available_at   AS currency_quote_available_at,
       rs.unlock_date,
       r.amount           AS reward_amount
FROM accounting.reward_status_data rs
         JOIN rewards r ON r.id = rs.reward_id
         LEFT JOIN iam.users u ON u.github_user_id = r.recipient_id
         LEFT JOIN accounting.payout_preferences pp ON pp.project_id = r.project_id AND pp.user_id = u.id
         LEFT JOIN accounting.billing_profiles bp ON bp.id = coalesce(r.billing_profile_id, pp.billing_profile_id) AND
                                                     bp.verification_status = 'VERIFIED'::accounting.verification_status
         LEFT JOIN currency_quote_available_at cqa ON cqa.base_id = r.currency_id;
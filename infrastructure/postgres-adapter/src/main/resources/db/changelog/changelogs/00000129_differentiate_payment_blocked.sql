DROP VIEW accounting.billing_profile_stats;
DROP VIEW accounting.reward_statuses;

DROP TYPE accounting.reward_status;

CREATE TYPE accounting.reward_status AS ENUM (
    'PENDING_SIGNUP',
    'PENDING_BILLING_PROFILE',
    'PENDING_VERIFICATION',
    'GEO_BLOCKED',
    'INDIVIDUAL_LIMIT_REACHED',
    'PAYOUT_INFO_MISSING',
    'LOCKED',
    'PENDING_REQUEST',
    'PROCESSING',
    'COMPLETE'
    );

CREATE VIEW accounting.reward_statuses AS
WITH billing_profile_payout_networks AS
         (SELECT coalesce(w.billing_profile_id, ba.billing_profile_id)                               AS billing_profile_id,
                 CASE WHEN w.billing_profile_id IS NOT NULL THEN array_agg(w.network) END ||
                 CASE WHEN ba.billing_profile_id IS NOT NULL THEN '{sepa}'::accounting.network[] END AS networks
          FROM accounting.wallets w
                   FULL OUTER JOIN accounting.bank_accounts ba ON ba.billing_profile_id = w.billing_profile_id
          GROUP BY w.billing_profile_id, ba.billing_profile_id),

     aggregated_reward_status_data AS
         (SELECT reward_id,
                 u.id IS NOT NULL                        as is_registered,
                 bp.type = 'INDIVIDUAL'                  as is_individual,
                 bp.verification_status = 'VERIFIED'     as kycb_verified,
                 coalesce(kyc.us_citizen, kyb.us_entity) as us_recipient,
                 c.code                                  as reward_currency,
                 coalesce(bpa.yearly_usd_total, 0)       as current_year_usd_total,
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
                   LEFT JOIN accounting.billing_profiles bp
                             ON bp.id = coalesce(r.billing_profile_id, pp.billing_profile_id)
                   LEFT JOIN accounting.kyc kyc on kyc.billing_profile_id = bp.id
                   LEFT JOIN accounting.kyb kyb on kyb.billing_profile_id = bp.id
                   LEFT JOIN billing_profile_payout_networks bppn on bppn.billing_profile_id = bp.id
                   LEFT JOIN accounting.billing_profiles_amounts bpa on bpa.billing_profile_id = bp.id)

SELECT reward_id,
       CASE
           WHEN s.paid_at IS NOT NULL THEN 'COMPLETE'::accounting.reward_status
           WHEN NOT s.is_registered THEN 'PENDING_SIGNUP'::accounting.reward_status
           WHEN s.is_individual IS NULL THEN 'PENDING_BILLING_PROFILE'::accounting.reward_status
           WHEN s.kycb_verified IS NOT TRUE THEN 'PENDING_VERIFICATION'::accounting.reward_status
           WHEN s.us_recipient IS TRUE AND s.reward_currency = 'STRK' THEN 'GEO_BLOCKED'::accounting.reward_status
           WHEN s.is_individual IS TRUE AND s.current_year_usd_total + s.reward_usd_equivalent > 5000
               THEN 'INDIVIDUAL_LIMIT_REACHED'::accounting.reward_status
           WHEN s.payout_info_filled IS NOT TRUE THEN 'PAYOUT_INFO_MISSING'::accounting.reward_status
           WHEN s.unlock_date IS NOT NULL AND s.unlock_date > NOW() THEN 'LOCKED'::accounting.reward_status
           WHEN s.sponsor_has_enough_fund IS NOT TRUE THEN 'LOCKED'::accounting.reward_status
           WHEN s.payment_requested_at IS NULL THEN 'PENDING_REQUEST'::accounting.reward_status
           ELSE 'PROCESSING'::accounting.reward_status
           END
           as status
FROM aggregated_reward_status_data s
;

CREATE VIEW accounting.billing_profile_stats AS
select bp.id                                                                       as billing_profile_id,
       count(r.*)                                                                  as reward_count,
       count(rs.reward_id) filter ( where rs.status = 'PENDING_REQUEST' )          as invoiceable_reward_count,
       count(rs.reward_id) filter ( where rs.status = 'PAYOUT_INFO_MISSING' ) > 0  as missing_payout_info,
       count(rs.reward_id) filter ( where rs.status = 'PENDING_VERIFICATION' ) > 0 as missing_verification,
       rpa.yearly_usd_total                                                        as current_year_payment_amount
from accounting.billing_profiles bp
         join accounting.billing_profiles_amounts rpa on rpa.billing_profile_id = bp.id
         left join rewards r on r.billing_profile_id = bp.id
         left join accounting.reward_statuses rs on rs.reward_id = r.id
         left join accounting.reward_status_data rsd on rsd.reward_id = r.id
group by bp.id, rpa.yearly_usd_total;

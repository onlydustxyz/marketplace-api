CREATE OR REPLACE VIEW accounting.reward_statuses AS
WITH aggregated_reward_status_data AS
         (SELECT r.*,
                 rs.*,
                 u.id                                             as recipient_user_id,
                 u.id IS NOT NULL                                 as is_registered,
                 bp.type = 'INDIVIDUAL'                           as is_individual,
                 bp.verification_status = 'VERIFIED'              as kycb_verified,
                 c.country_restrictions                           as country_restrictions,
                 ARRAY [
                     case when kyc.considered_us_person_questionnaire or kyb.us_entity THEN 'USA' end,
                     kyc.country,
                     kyc.id_document_country_code,
                     kyb.country
                     ]                                            as kycb_countries,
                 coalesce(bpa.yearly_usd_total, 0)                as current_year_usd_total,
                 (
                     (select array_agg(w.network)
                      from accounting.wallets w
                      where w.billing_profile_id = bp.id) ||
                     (select '{SEPA}'::accounting.network[]
                      from accounting.bank_accounts ba
                      where ba.billing_profile_id = bp.id)
                     ) @> rs.networks                             as payout_info_filled,
                 coalesce(cipl.usd_yearly_individual_limit, 5001) as usd_yearly_individual_limit
          FROM accounting.reward_status_data rs
                   JOIN rewards r on r.id = rs.reward_id
                   JOIN currencies c on c.id = r.currency_id
                   LEFT JOIN iam.users u on u.github_user_id = r.recipient_id
                   LEFT JOIN accounting.payout_preferences pp on pp.project_id = r.project_id and pp.user_id = u.id
                   LEFT JOIN accounting.billing_profiles bp
                             ON bp.id = coalesce(r.billing_profile_id, pp.billing_profile_id)
                   LEFT JOIN accounting.kyc kyc on kyc.billing_profile_id = bp.id
                   LEFT JOIN accounting.kyb kyb on kyb.billing_profile_id = bp.id
                   LEFT JOIN accounting.country_individual_payment_limits cipl ON cipl.country_code = kyc.country
                   LEFT JOIN accounting.billing_profiles_amounts bpa on bpa.billing_profile_id = bp.id)

SELECT s.reward_id,
       CASE
           WHEN s.paid_at IS NOT NULL THEN 'COMPLETE'::accounting.reward_status
           WHEN s.invoice_received_at IS NOT NULL THEN 'PROCESSING'::accounting.reward_status
           WHEN NOT s.is_registered THEN 'PENDING_SIGNUP'::accounting.reward_status
           WHEN s.is_individual IS NULL THEN 'PENDING_BILLING_PROFILE'::accounting.reward_status
           WHEN s.kycb_verified IS NOT TRUE THEN 'PENDING_VERIFICATION'::accounting.reward_status
           WHEN s.kycb_countries && s.country_restrictions THEN 'GEO_BLOCKED'::accounting.reward_status
           WHEN s.is_individual IS TRUE AND s.current_year_usd_total + s.amount_usd_equivalent >= s.usd_yearly_individual_limit
               THEN 'INDIVIDUAL_LIMIT_REACHED'::accounting.reward_status
           WHEN s.payout_info_filled IS NOT TRUE THEN 'PAYOUT_INFO_MISSING'::accounting.reward_status
           WHEN s.unlock_date IS NOT NULL AND s.unlock_date > NOW() THEN 'LOCKED'::accounting.reward_status
           WHEN s.sponsor_has_enough_fund IS NOT TRUE THEN 'LOCKED'::accounting.reward_status
           ELSE 'PENDING_REQUEST'::accounting.reward_status
           END
                                as status,
       s.project_id,
       s.requestor_id,
       s.recipient_id,
       s.amount,
       s.requested_at,
       s.invoice_id,
       s.currency_id,
       s.payment_notified_at,
       s.billing_profile_id,
       s.sponsor_has_enough_fund,
       s.unlock_date,
       s.invoice_received_at,
       s.paid_at,
       s.amount_usd_equivalent,
       s.networks,
       s.usd_conversion_rate,
       s.recipient_user_id      as recipient_user_id,
       s.is_registered          as recipient_is_registered,
       s.is_individual          as recipient_bp_is_individual,
       s.current_year_usd_total as recipient_bp_current_year_usd_total,
       s.kycb_verified          as recipient_kycb_verified,
       s.kycb_countries         as recipient_kycb_countries,
       s.country_restrictions   as currency_country_restrictions,
       s.payout_info_filled     as recipient_payout_info_filled
FROM aggregated_reward_status_data s
;
CREATE OR REPLACE VIEW accounting.billing_profiles_amounts AS
SELECT r.billing_profile_id,
       sum(rsd.amount_usd_equivalent) as yearly_usd_total
FROM rewards r
         JOIN accounting.reward_status_data rsd
              ON r.id = rsd.reward_id AND (
                  -- paid this year
                  (rsd.paid_at BETWEEN date_trunc('year'::text, now()) AND (date_trunc('year'::text, now()) + '1 year'::interval))
                      -- or not paid yet, but has an invoice
                      OR (rsd.invoice_received_at IS NOT NULL AND rsd.paid_at IS NULL))
GROUP BY r.billing_profile_id;



CREATE OR REPLACE VIEW accounting.billing_profile_stats AS
select bp.id                                                                                                 as billing_profile_id,
       count(r.*)                                                                                            as reward_count,
       count(rs.reward_id) filter ( where rs.status = 'PENDING_REQUEST' )                                    as invoiceable_reward_count,
       count(rs.reward_id) filter ( where rs.status = 'PAYOUT_INFO_MISSING' ) > 0                            as missing_payout_info,
       count(rs.reward_id) filter ( where rs.status = 'PENDING_VERIFICATION' ) > 0                           as missing_verification,
       count(rs.reward_id) filter ( where rs.status = 'INDIVIDUAL_LIMIT_REACHED' ) > 0                       as individual_limit_reached,
       coalesce(rpa.yearly_usd_total, 0)                                                                     as current_year_payment_amount,
       bp.type != 'INDIVIDUAL' and (bp.invoice_mandate_accepted_at is null or
                                    bp.invoice_mandate_accepted_at < gs.invoice_mandate_latest_version_date) as mandate_acceptance_outdated,
       case
           when kyc.country is not null then
               coalesce(cipl.usd_yearly_individual_limit, 5001)
           end                                                                                               as current_year_payment_limit
from accounting.billing_profiles bp
         left join accounting.billing_profiles_amounts rpa on rpa.billing_profile_id = bp.id
         left join rewards r on r.billing_profile_id = bp.id
         left join accounting.reward_statuses rs on rs.reward_id = r.id
         left join accounting.reward_status_data rsd on rsd.reward_id = r.id
         left join accounting.kyc kyc on kyc.billing_profile_id = bp.id
         left join accounting.country_individual_payment_limits cipl ON cipl.country_code = kyc.country
         cross join global_settings gs
group by bp.id,
         rpa.yearly_usd_total,
         gs.invoice_mandate_latest_version_date,
         kyc.country,
         cipl.usd_yearly_individual_limit;



CREATE OR REPLACE VIEW accounting.reward_statuses AS
WITH aggregated_reward_status_data AS
         (SELECT reward_id,
                 u.id IS NOT NULL                                 as is_registered,
                 bp.type = 'INDIVIDUAL'                           as is_individual,
                 bp.verification_status = 'VERIFIED'              as kycb_verified,
                 ARRAY [
                     case when kyc.considered_us_person_questionnaire or kyb.us_entity THEN 'USA' end,
                     kyc.country,
                     kyc.id_document_country_code,
                     kyb.country
                     ]                                            as kycb_countries,
                 c.country_restrictions                           as country_restrictions,
                 coalesce(bpa.yearly_usd_total, 0)                as current_year_usd_total,
                 rs.amount_usd_equivalent                         as reward_usd_equivalent,
                 (
                     (select array_agg(w.network)
                      from accounting.wallets w
                      where w.billing_profile_id = bp.id) ||
                     (select '{SEPA}'::accounting.network[]
                      from accounting.bank_accounts ba
                      where ba.billing_profile_id = bp.id)
                     ) @> rs.networks                             as payout_info_filled,
                 rs.sponsor_has_enough_fund,
                 rs.unlock_date,
                 rs.invoice_received_at                           as payment_requested_at,
                 rs.paid_at,
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

SELECT reward_id,
       CASE
           WHEN s.paid_at IS NOT NULL THEN 'COMPLETE'::accounting.reward_status
           WHEN s.payment_requested_at IS NOT NULL THEN 'PROCESSING'::accounting.reward_status
           WHEN NOT s.is_registered THEN 'PENDING_SIGNUP'::accounting.reward_status
           WHEN s.is_individual IS NULL THEN 'PENDING_BILLING_PROFILE'::accounting.reward_status
           WHEN s.kycb_verified IS NOT TRUE THEN 'PENDING_VERIFICATION'::accounting.reward_status
           WHEN s.kycb_countries && s.country_restrictions THEN 'GEO_BLOCKED'::accounting.reward_status
           WHEN s.is_individual IS TRUE AND s.current_year_usd_total + s.reward_usd_equivalent >= s.usd_yearly_individual_limit
               THEN 'INDIVIDUAL_LIMIT_REACHED'::accounting.reward_status
           WHEN s.payout_info_filled IS NOT TRUE THEN 'PAYOUT_INFO_MISSING'::accounting.reward_status
           WHEN s.unlock_date IS NOT NULL AND s.unlock_date > NOW() THEN 'LOCKED'::accounting.reward_status
           WHEN s.sponsor_has_enough_fund IS NOT TRUE THEN 'LOCKED'::accounting.reward_status
           ELSE 'PENDING_REQUEST'::accounting.reward_status
           END
           as status
FROM aggregated_reward_status_data s
;
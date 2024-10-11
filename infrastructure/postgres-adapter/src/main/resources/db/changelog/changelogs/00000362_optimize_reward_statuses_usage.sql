CREATE FUNCTION accounting.billing_profile_current_year_usd_amount(billingProfileId UUID)
    RETURNS NUMERIC AS
$$
SELECT sum((select rsd.amount_usd_equivalent
            from accounting.reward_status_data rsd
            where rsd.reward_id = r.id
              AND (rsd.paid_at >= date_trunc('year'::text, now()) AND rsd.paid_at <= (date_trunc('year'::text, now()) + '1 year'::interval) OR
                   rsd.invoice_received_at IS NOT NULL AND rsd.paid_at IS NULL))) AS yearly_usd_total
FROM rewards r
where r.billing_profile_id = billingProfileId
group by r.billing_profile_id
$$ LANGUAGE SQL STABLE
                PARALLEL SAFE;


create unique index on rewards (id, billing_profile_id);
create unique index on rewards (billing_profile_id, id);
create unique index on accounting.reward_status_data (reward_id, invoice_received_at, paid_at, amount_usd_equivalent);

create unique index on iam.users (id, github_user_id);
create unique index on iam.users (github_user_id, id);

create unique index on accounting.billing_profiles (id, type, verification_status);
create unique index on accounting.kyc (billing_profile_id, considered_us_person_questionnaire, country, id_document_country_code);
create unique index on accounting.kyb (billing_profile_id, us_entity, country);
create unique index on accounting.country_individual_payment_limits (country_code, usd_yearly_individual_limit);
create unique index on currencies (id, country_restrictions);



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
                 case
                     when bp.type = 'INDIVIDUAL'
                         then coalesce(accounting.billing_profile_current_year_usd_amount(bp.id), 0)
                     end                                          as current_year_usd_total,
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
                   LEFT JOIN accounting.country_individual_payment_limits cipl ON cipl.country_code = kyc.country)

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
       s.recipient_user_id           as recipient_user_id,
       s.is_registered               as recipient_is_registered,
       s.is_individual               as recipient_bp_is_individual,
       s.current_year_usd_total      as recipient_bp_current_year_usd_total,
       s.usd_yearly_individual_limit as recipient_bp_usd_yearly_individual_limit,
       s.kycb_verified               as recipient_kycb_verified,
       s.kycb_countries              as recipient_kycb_countries,
       s.country_restrictions        as currency_country_restrictions,
       s.payout_info_filled          as recipient_payout_info_filled
FROM aggregated_reward_status_data s
;



CREATE OR REPLACE VIEW accounting.billing_profile_stats AS
select bp.id                                                                                                 as billing_profile_id,
       count(r.reward_id)                                                                                    as reward_count,
       count(r.reward_id) filter ( where r.status = 'PENDING_REQUEST' )                                      as invoiceable_reward_count,
       count(r.reward_id) filter ( where r.status = 'PAYOUT_INFO_MISSING' ) > 0                              as missing_payout_info,
       count(r.reward_id) filter ( where r.status = 'PENDING_VERIFICATION' ) > 0                             as missing_verification,
       count(r.reward_id) filter ( where r.status = 'INDIVIDUAL_LIMIT_REACHED' ) > 0                         as individual_limit_reached,
       coalesce(max(r.recipient_bp_current_year_usd_total), 0)                                               as current_year_payment_amount,
       bp.type != 'INDIVIDUAL' and (bp.invoice_mandate_accepted_at is null or
                                    bp.invoice_mandate_accepted_at < gs.invoice_mandate_latest_version_date) as mandate_acceptance_outdated,
       case
           when kyc.country is not null then
               coalesce(cipl.usd_yearly_individual_limit, 5001)
           end                                                                                               as current_year_payment_limit
from accounting.billing_profiles bp
         left join accounting.reward_statuses r on r.billing_profile_id = bp.id
         left join accounting.kyc kyc on kyc.billing_profile_id = bp.id
         left join accounting.country_individual_payment_limits cipl ON cipl.country_code = kyc.country
         cross join global_settings gs
group by bp.id,
         gs.invoice_mandate_latest_version_date,
         kyc.country,
         cipl.usd_yearly_individual_limit;



DROP VIEW accounting.billing_profiles_amounts;

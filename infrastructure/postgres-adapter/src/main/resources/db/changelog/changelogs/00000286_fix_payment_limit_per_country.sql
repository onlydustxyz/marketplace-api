CREATE OR REPLACE VIEW accounting.billing_profile_stats AS
select bp.id                                                                                                 as billing_profile_id,
       count(r.*)                                                                                            as reward_count,
       count(rs.reward_id) filter ( where rs.status = 'PENDING_REQUEST' )                                    as invoiceable_reward_count,
       count(rs.reward_id) filter ( where rs.status = 'PAYOUT_INFO_MISSING' ) > 0                            as missing_payout_info,
       count(rs.reward_id) filter ( where rs.status = 'PENDING_VERIFICATION' ) > 0                           as missing_verification,
       count(rs.reward_id) filter ( where rs.status = 'INDIVIDUAL_LIMIT_REACHED' ) > 0                       as individual_limit_reached,
       rpa.yearly_usd_total                                                                                  as current_year_payment_amount,
       bp.type != 'INDIVIDUAL' and (bp.invoice_mandate_accepted_at is null or
                                    bp.invoice_mandate_accepted_at < gs.invoice_mandate_latest_version_date) as mandate_acceptance_outdated,
       case
           when kyc.country is not null then
               coalesce(cipl.usd_yearly_individual_limit, 5001)
           end                                                                                               as current_year_payment_limit
from accounting.billing_profiles bp
         join accounting.billing_profiles_amounts rpa on rpa.billing_profile_id = bp.id
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

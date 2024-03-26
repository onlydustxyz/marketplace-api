CREATE VIEW accounting.billing_profile_stats AS
select bp.id                                                                                                                     as billing_profile_id,
       count(r.*)                                                                                                                as reward_count,
       count(rs.reward_id) filter ( where rs.status = 'PENDING_REQUEST' )                                                        as invoiceable_reward_count,
       count(rs.reward_id) filter ( where rs.status = 'PAYOUT_INFO_MISSING' ) > 0                                                as missing_payout_info,
       count(rs.reward_id) filter ( where rs.status = 'PENDING_VERIFICATION' ) > 0                                               as missing_verification,
       coalesce(sum(rsd.amount_usd_equivalent)
                filter ( where rs.status = 'PROCESSING' OR DATE_PART('isoyear', rsd.paid_at) = DATE_PART('isoyear', NOW()) ), 0) as current_year_payment_amount
from accounting.billing_profiles bp
         left join rewards r on r.billing_profile_id = bp.id
         left join accounting.reward_statuses rs on rs.reward_id = r.id
         left join accounting.reward_status_data rsd on rsd.reward_id = r.id
group by bp.id;
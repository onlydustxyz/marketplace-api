drop view accounting.billing_profile_stats;


CREATE VIEW accounting.billing_profile_stats AS
select bp.id                                                                                                 as billing_profile_id,
       count(r.*)                                                                                            as reward_count,
       count(rs.reward_id) filter ( where rs.status = 'PENDING_REQUEST' )                                    as invoiceable_reward_count,
       count(rs.reward_id) filter ( where rs.status = 'PAYOUT_INFO_MISSING' ) > 0                            as missing_payout_info,
       count(rs.reward_id) filter ( where rs.status = 'PENDING_VERIFICATION' ) > 0                           as missing_verification,
       count(rs.reward_id) filter ( where rs.status = 'INDIVIDUAL_LIMIT_REACHED' ) > 0                       as individual_limit_reached,
       rpa.yearly_usd_total                                                                                  as current_year_payment_amount,
       bp.type != 'INDIVIDUAL' and (bp.invoice_mandate_accepted_at is null or
                                    bp.invoice_mandate_accepted_at < gs.invoice_mandate_latest_version_date) as mandate_acceptance_outdated
from accounting.billing_profiles bp
         join accounting.billing_profiles_amounts rpa on rpa.billing_profile_id = bp.id
         left join rewards r on r.billing_profile_id = bp.id
         left join accounting.reward_statuses rs on rs.reward_id = r.id
         left join accounting.reward_status_data rsd on rsd.reward_id = r.id
         cross join global_settings gs
group by bp.id,
         rpa.yearly_usd_total,
         gs.invoice_mandate_latest_version_date;

CREATE VIEW accounting.all_billing_profile_users AS
SELECT bpu.billing_profile_id as billing_profile_id,
       bpu.user_id            as user_id,
       u.github_user_id       as github_user_id,
       bpu.role               as role,
       true                   as invitation_accepted
FROM accounting.billing_profiles_users bpu
         JOIN iam.all_users u on bpu.user_id = u.user_id
UNION
SELECT bpui.billing_profile_id as billing_profile_id,
       u.user_id               as user_id,
       bpui.github_user_id     as github_user_id,
       bpui.role               as role,
       bpui.accepted           as invitation_accepted
FROM accounting.billing_profiles_user_invitations bpui
         JOIN iam.all_users u on bpui.github_user_id = u.github_user_id;
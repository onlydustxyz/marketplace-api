alter table public.rewards
    add column billing_profile_id uuid;

with invoices as (select i.id, i.billing_profile_id
                  from accounting.invoices i)
update public.rewards
set billing_profile_id = invoices.billing_profile_id
from invoices
where invoices.id = invoice_id;

with bp as (select r.id as reward_id, bp.id as billing_profile_id
            from accounting.reward_statuses rs
                     join rewards r on r.id = rs.reward_id
                     join iam.users u on u.github_user_id = r.recipient_id
                     join user_billing_profile_types ubpt on u.id = ubpt.user_id and ubpt.billing_profile_type = 'COMPANY'
                     join accounting.billing_profiles_users bpu on bpu.user_id = u.id and bpu.role = 'ADMIN'
                     join accounting.billing_profiles bp on bp.id = bpu.billing_profile_id and bp.type = 'SELF_EMPLOYED'
            where rs.status = 'COMPLETE'
              and r.billing_profile_id is null)
update public.rewards
set billing_profile_id = bp.billing_profile_id
from bp
where bp.reward_id = id;

with bp as (select r.id as reward_id, bp.id as billing_profile_id
            from accounting.reward_statuses rs
                     join rewards r on r.id = rs.reward_id
                     join iam.users u on u.github_user_id = r.recipient_id
                     join user_billing_profile_types ubpt on u.id = ubpt.user_id and ubpt.billing_profile_type = 'INDIVIDUAL'
                     join accounting.billing_profiles_users bpu on bpu.user_id = u.id and bpu.role = 'ADMIN'
                     join accounting.billing_profiles bp on bp.id = bpu.billing_profile_id and bp.type = 'INDIVIDUAL'
            where rs.status = 'COMPLETE'
              and r.billing_profile_id is null)
update public.rewards
set billing_profile_id = bp.billing_profile_id
from bp
where bp.reward_id = id;

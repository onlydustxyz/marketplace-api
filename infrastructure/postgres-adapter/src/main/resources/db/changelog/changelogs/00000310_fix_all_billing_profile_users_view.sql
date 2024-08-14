DROP VIEW accounting.all_billing_profile_users;

CREATE VIEW accounting.all_billing_profile_users (github_user_id, billing_profile_id, role, user_id, invitation_accepted) as
with all_bp_users as (select u.github_user_id, bpu.billing_profile_id, bpu.role, u.id user_id, true as accepted
                      from accounting.billing_profiles_users bpu
                               join iam.users u on u.id = bpu.user_id
                      union
                      select bpui.github_user_id, bpui.billing_profile_id, bpui.role, u.id user_id, bpui.accepted
                      from accounting.billing_profiles_user_invitations bpui
                               left join iam.users u on u.github_user_id = bpui.github_user_id)
select distinct on (a.github_user_id, a.billing_profile_id) a.*
from all_bp_users a;

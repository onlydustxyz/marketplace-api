CREATE OR REPLACE VIEW accounting.all_billing_profile_users AS
SELECT DISTINCT ON (coalesce(bpu.billing_profile_id, bpui.billing_profile_id), u.user_id) coalesce(bpu.billing_profile_id, bpui.billing_profile_id) AS billing_profile_id,
                                                                                          u.user_id,
                                                                                          u.github_user_id,
                                                                                          coalesce(bpu.role, bpui.role)                             AS role,
                                                                                          coalesce(bpui.accepted, TRUE)                             AS invitation_accepted
FROM accounting.billing_profiles_users bpu
         FULL JOIN accounting.billing_profiles_user_invitations bpui ON bpui.billing_profile_id = bpu.billing_profile_id
         JOIN iam.all_users u ON bpu.user_id = u.user_id OR bpui.github_user_id = u.github_user_id
;
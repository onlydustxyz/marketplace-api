create view iam.all_users as
select u.id                                                         as user_id,
       coalesce(ga.id, u.github_user_id)                            as github_user_id,
       coalesce(ga.login, u.github_login)                           as login,
       coalesce(upi.avatar_url, ga.avatar_url, u.github_avatar_url) as avatar_url
from iam.users u
         left join user_profile_info upi on u.id = upi.id
         full outer join indexer_exp.github_accounts ga on ga.id = u.github_user_id and ga.type = 'USER';


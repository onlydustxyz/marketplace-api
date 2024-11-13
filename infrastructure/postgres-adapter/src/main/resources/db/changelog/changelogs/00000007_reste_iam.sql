create table if not exists iam.user_projects_notification_settings
(
    user_id                   uuid                    not null
        references iam.users,
    project_id                uuid                    not null
        references public.projects,
    on_good_first_issue_added boolean                 not null,
    tech_created_at           timestamptz default now() not null,
    tech_updated_at           timestamptz default now() not null,
    primary key (user_id, project_id)
);

create trigger user_projects_notification_settings_set_tech_updated_at
    before update
    on iam.user_projects_notification_settings
    for each row
execute procedure public.set_tech_updated_at();


create or replace view iam.all_users
            (user_id, github_user_id, login, avatar_url, email, bio, signed_up_at, signed_up_on_github_at) as
SELECT u.id                                                         AS user_id,
       COALESCE(ga.id, u.github_user_id)                            AS github_user_id,
       COALESCE(ga.login, u.github_login)                           AS login,
       COALESCE(upi.avatar_url, ga.avatar_url, u.github_avatar_url) AS avatar_url,
       u.email,
       COALESCE(upi.bio, ga.bio)                                    AS bio,
       u.created_at::timestamptz                       AS signed_up_at,
       ga.created_at::timestamptz                      AS signed_up_on_github_at
FROM iam.users u
         FULL JOIN indexer_exp.github_accounts ga ON ga.id = u.github_user_id
         LEFT JOIN public.user_profile_info upi ON u.id = upi.id;

create or replace view iam.all_indexed_users
            (user_id, github_user_id, login, avatar_url, email, bio, signed_up_at, signed_up_on_github_at) as
SELECT u.id                                                         AS user_id,
       ga.id                                                        AS github_user_id,
       ga.login,
       COALESCE(upi.avatar_url, ga.avatar_url, u.github_avatar_url) AS avatar_url,
       u.email,
       COALESCE(upi.bio, ga.bio)                                    AS bio,
       u.created_at::timestamptz                       AS signed_up_at,
       ga.created_at::timestamptz                      AS signed_up_on_github_at
FROM indexer_exp.github_accounts ga
         LEFT JOIN iam.users u ON u.github_user_id = ga.id
         LEFT JOIN public.user_profile_info upi ON u.id = upi.id;
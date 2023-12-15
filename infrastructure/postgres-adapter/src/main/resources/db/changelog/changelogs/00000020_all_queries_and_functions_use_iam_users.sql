CREATE OR REPLACE FUNCTION user_avatar_url(github_user_id bigint, fallback_url text)
    RETURNS TEXT AS
$$
SELECT coalesce(
               (SELECT upi.avatar_url
                FROM user_profile_info upi
                         JOIN iam.users u ON u.id = upi.id
                WHERE u.github_user_id = $1),
               fallback_url
       )
$$ LANGUAGE sql STABLE;


CREATE OR REPLACE VIEW registered_users AS
SELECT u.id,
       u.github_user_id,
       COALESCE(ga.login, u.github_login)                                   AS login,
       COALESCE(ga.avatar_url, u.github_avatar_url)                         AS avatar_url,
       COALESCE(ga.html_url, 'https://github.com/'::text || u.github_login) AS html_url,
       u.email::citext                                                      AS email,
       u.last_seen_at                                                       AS last_seen,
       CASE WHEN 'ADMIN' = ANY (u.roles) THEN true ELSE false END           AS admin
FROM iam.users u
         LEFT JOIN indexer_exp.github_accounts ga ON ga.id = u.github_user_id;
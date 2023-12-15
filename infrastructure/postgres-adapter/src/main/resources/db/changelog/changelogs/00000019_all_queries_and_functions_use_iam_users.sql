CREATE OR REPLACE FUNCTION user_avatar_url(github_user_id bigint, fallback_url text)
    RETURNS TEXT AS
$$
SELECT coalesce(
               (SELECT upi.avatar_url
                FROM user_profile_info upi
                WHERE upi.id = (SELECT u.id FROM iam.users u WHERE u.github_user_id = $1)),
               fallback_url
       )
$$ LANGUAGE sql STABLE;
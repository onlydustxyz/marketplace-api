CREATE OR REPLACE VIEW accounting.all_billing_profile_users (github_user_id, billing_profile_id, role, user_id, invitation_accepted) as
SELECT u.github_user_id,
       bpu.billing_profile_id,
       bpu.role,
       u.id AS user_id,
       true AS invitation_accepted
FROM accounting.billing_profiles_users bpu
         JOIN iam.users u ON u.id = bpu.user_id
UNION
SELECT bpui.github_user_id,
       bpui.billing_profile_id,
       bpui.role,
       u.id  AS user_id,
       false as invitation_accepted
FROM accounting.billing_profiles_user_invitations bpui
         LEFT JOIN iam.users u ON u.github_user_id = bpui.github_user_id
WHERE bpui.accepted IS FALSE;



CREATE OR REPLACE FUNCTION accounting.usd_quote_at(currency_id UUID, at timestamp with time zone)
    RETURNS NUMERIC AS
$$
SELECT price
FROM accounting.historical_quotes hq
         JOIN currencies usd ON usd.id = hq.target_id and usd.code = 'USD'
WHERE hq.base_id = currency_id
  AND hq.timestamp <= at
ORDER BY hq.timestamp DESC
LIMIT 1
$$ LANGUAGE SQL STABLE
                PARALLEL SAFE;


CREATE OR REPLACE FUNCTION accounting.usd_equivalent_at(amount numeric, currency_id UUID, at timestamp with time zone)
    RETURNS NUMERIC AS
$$
SELECT amount * accounting.usd_quote_at(currency_id, at);
$$ LANGUAGE SQL STABLE
                PARALLEL SAFE;

ALTER VIEW bi.weekly_rewards_creation_stats RENAME TO weekly_rewards_creation_stats_per_currency;

ALTER VIEW bi.monthly_rewards_creation_stats RENAME TO monthly_rewards_creation_stats_per_currency;


CREATE VIEW bi.weekly_rewards_creation_stats AS
SELECT DATE_TRUNC('week', r.requested_at) AS creation_date,
       SUM(rsd.amount_usd_equivalent)     AS total_usd_amount,
       COUNT(DISTINCT r.id)               AS rewards_count,
       COUNT(DISTINCT r.recipient_id)     AS contributors_count,
       COUNT(DISTINCT r.requestor_id)     AS project_leads_count
FROM rewards r
         JOIN currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
GROUP BY creation_date;


CREATE VIEW bi.monthly_rewards_creation_stats AS
SELECT DATE_TRUNC('month', r.requested_at) AS creation_date,
       SUM(rsd.amount_usd_equivalent)      AS total_usd_amount,
       COUNT(DISTINCT r.id)                AS rewards_count,
       COUNT(DISTINCT r.recipient_id)      AS contributors_count,
       COUNT(DISTINCT r.requestor_id)      AS project_leads_count
FROM rewards r
         JOIN currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
GROUP BY creation_date;



CREATE VIEW bi.daily_users_creation_stats AS
SELECT DATE_TRUNC('day', u.created_at) AS creation_date,
       COUNT(u.id)                     AS users_count
FROM iam.users u
GROUP BY creation_date;


CREATE VIEW bi.daily_projects_creation_stats AS
SELECT DATE_TRUNC('day', p.created_at) AS creation_date,
       COUNT(p.project_id)             AS projects_count
FROM project_details p
GROUP BY creation_date;
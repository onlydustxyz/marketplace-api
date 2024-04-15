CREATE SCHEMA bi;

-- -------------------------------------------------------------
-- REWARDS

CREATE VIEW bi.weekly_rewards_creation_stats AS
SELECT DATE_TRUNC('week', r.requested_at) AS creation_date,
       c.code                             AS currency,
       SUM(rsd.amount_usd_equivalent)     AS total_usd_amount,
       SUM(r.amount)                      AS total_amount,
       COUNT(DISTINCT r.id)               AS rewards_count,
       COUNT(DISTINCT r.recipient_id)     AS contributors_count,
       COUNT(DISTINCT r.requestor_id)     AS project_leads_count
FROM rewards r
         JOIN currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
GROUP BY creation_date, c.code;


CREATE VIEW bi.monthly_rewards_creation_stats AS
SELECT DATE_TRUNC('month', r.requested_at) AS creation_date,
       c.code                              AS currency,
       SUM(rsd.amount_usd_equivalent)      AS total_usd_amount,
       SUM(r.amount)                       AS total_amount,
       COUNT(DISTINCT r.id)                AS rewards_count,
       COUNT(DISTINCT r.recipient_id)      AS contributors_count,
       COUNT(DISTINCT r.requestor_id)      AS project_leads_count
FROM rewards r
         JOIN currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
GROUP BY creation_date, c.code;


-- -------------------------------------------------------------
-- USERS

CREATE VIEW bi.weekly_users_creation_stats AS
SELECT DATE_TRUNC('week', u.created_at) AS creation_date,
       COUNT(u.id)                      AS users_count
FROM iam.users u
GROUP BY creation_date;


CREATE VIEW bi.monthly_users_creation_stats AS
SELECT DATE_TRUNC('month', u.created_at) AS creation_date,
       COUNT(u.id)                       AS users_count
FROM iam.users u
GROUP BY creation_date;

-- -------------------------------------------------------------
-- PROJECTS

CREATE VIEW bi.weekly_projects_creation_stats AS
SELECT DATE_TRUNC('week', p.created_at) AS creation_date,
       COUNT(p.project_id)              AS projects_count
FROM project_details p
GROUP BY creation_date;


CREATE VIEW bi.monthly_projects_creation_stats AS
SELECT DATE_TRUNC('month', p.created_at) AS creation_date,
       COUNT(p.project_id)               AS projects_count
FROM project_details p
GROUP BY creation_date;


-- -------------------------------------------------------------
-- CONTRIBUTIONS

CREATE VIEW bi.weekly_contributions_creation_stats AS
SELECT DATE_TRUNC('week', c.created_at)                                   AS creation_date,
       COUNT(DISTINCT c.contributor_id)                                   AS contributors_count,
       COUNT(DISTINCT c.contributor_id) FILTER ( WHERE u.id IS NOT NULL ) AS registered_contributors_count,
       COUNT(DISTINCT c.contributor_id) FILTER ( WHERE u.id IS NULL )     AS external_contributors_count
FROM indexer_exp.contributions c
         LEFT JOIN iam.users u ON u.github_user_id = c.contributor_id
GROUP BY creation_date;


CREATE VIEW bi.monthly_contributions_creation_stats AS
SELECT DATE_TRUNC('month', c.created_at)                                  AS creation_date,
       COUNT(DISTINCT c.contributor_id)                                   AS contributors_count,
       COUNT(DISTINCT c.contributor_id) FILTER ( WHERE u.id IS NOT NULL ) AS registered_contributors_count,
       COUNT(DISTINCT c.contributor_id) FILTER ( WHERE u.id IS NULL )     AS external_contributors_count
FROM indexer_exp.contributions c
         LEFT JOIN iam.users u ON u.github_user_id = c.contributor_id
GROUP BY creation_date;

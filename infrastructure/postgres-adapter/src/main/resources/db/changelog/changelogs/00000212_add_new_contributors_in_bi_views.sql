DROP VIEW bi.weekly_rewards_creation_stats;
DROP VIEW bi.monthly_rewards_creation_stats;

CREATE VIEW bi.weekly_rewards_creation_stats AS
WITH first_rewards AS (SELECT recipient_id, date_trunc('week', min(requested_at)) as date
                       FROM rewards
                       GROUP BY recipient_id)
SELECT DATE_TRUNC('week', r.requested_at)                                AS creation_date,
       SUM(rsd.amount_usd_equivalent)                                    AS total_usd_amount,
       COUNT(DISTINCT r.id)                                              AS rewards_count,
       COUNT(DISTINCT r.recipient_id)                                    AS contributors_count,
       count(DISTINCT r.recipient_id)
       FILTER (WHERE fr.date = date_trunc('week'::text, r.requested_at)) AS new_contributors_count,
       COUNT(DISTINCT r.requestor_id)                                    AS project_leads_count
FROM rewards r
         JOIN currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
         JOIN first_rewards fr ON fr.recipient_id = r.recipient_id
GROUP BY creation_date;


CREATE VIEW bi.monthly_rewards_creation_stats AS
WITH first_rewards AS (SELECT recipient_id, date_trunc('month', min(requested_at)) as date
                       FROM rewards
                       GROUP BY recipient_id)
SELECT DATE_TRUNC('month', r.requested_at)                                AS creation_date,
       SUM(rsd.amount_usd_equivalent)                                     AS total_usd_amount,
       COUNT(DISTINCT r.id)                                               AS rewards_count,
       COUNT(DISTINCT r.recipient_id)                                     AS contributors_count,
       count(DISTINCT r.recipient_id)
       FILTER (WHERE fr.date = date_trunc('month'::text, r.requested_at)) AS new_contributors_count,
       COUNT(DISTINCT r.requestor_id)                                     AS project_leads_count
FROM rewards r
         JOIN currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
         JOIN first_rewards fr ON fr.recipient_id = r.recipient_id
GROUP BY creation_date;
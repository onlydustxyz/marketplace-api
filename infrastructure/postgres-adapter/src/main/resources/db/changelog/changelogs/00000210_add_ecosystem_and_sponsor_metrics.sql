create view bi.monthly_contributors as
with monthly AS (SELECT generate_series(date_trunc('month', '2023-01-01'::date),
                                        date_trunc('month', CURRENT_DATE),
                                        INTERVAL '1 month') AS date)
select m.date                                 as date,
       c.repo_id                              as repo_id,
       c.contributor_id                       as contributor_id,
       date_trunc('month', min(c.created_at)) as first,
       date_trunc('month', max(c.created_at)) as latest
from monthly m
         left join indexer_exp.contributions c
                   on date_trunc('month', c.created_at) <= m.date
group by m.date, c.repo_id, c.contributor_id;

create view bi.monthly_contributions_stats_per_ecosystem as
SELECT e.name                                                                                  as ecosystem,
       c.date                                                                                  as creation_date,
       count(distinct c.contributor_id) filter ( where c.latest = c.date )                     as contributors_count,
       count(distinct c.contributor_id) filter ( where c.first = c.date )                      as new_contributors_count,
       count(distinct c.contributor_id) filter ( where c.latest = c.date - interval '1 month') as churned_contributors_count
FROM bi.monthly_contributors c
         join project_github_repos pgr on pgr.github_repo_id = c.repo_id
         join projects_ecosystems pe on pe.project_id = pgr.project_id
         join ecosystems e on e.id = pe.ecosystem_id
group by e.name, c.date
;

create view bi.monthly_contributions_stats_per_sponsor as
SELECT s.name                                                                                  as sponsor,
       c.date                                                                                  as creation_date,
       count(distinct c.contributor_id) filter ( where c.latest = c.date )                     as contributors_count,
       count(distinct c.contributor_id) filter ( where c.first = c.date )                      as new_contributors_count,
       count(distinct c.contributor_id) filter ( where c.latest = c.date - interval '1 month') as churned_contributors_count
FROM bi.monthly_contributors c
         join project_github_repos pgr on pgr.github_repo_id = c.repo_id
         join projects_sponsors ps on ps.project_id = pgr.project_id
         join sponsors s on s.id = ps.sponsor_id
group by s.name, c.date
;



create view bi.weekly_contributors as
with weekly AS (SELECT generate_series(date_trunc('week', '2023-01-01'::date),
                                       date_trunc('week', CURRENT_DATE),
                                       INTERVAL '1 week') AS date)
select w.date                                as date,
       c.repo_id                             as repo_id,
       c.contributor_id                      as contributor_id,
       date_trunc('week', min(c.created_at)) as first,
       date_trunc('week', max(c.created_at)) as latest
from weekly w
         left join indexer_exp.contributions c
                   on date_trunc('week', c.created_at) <= w.date
group by w.date, c.repo_id, c.contributor_id;

create view bi.weekly_contributions_stats_per_ecosystem as
SELECT e.name                                                                                 as ecosystem,
       c.date                                                                                 as creation_date,
       count(distinct c.contributor_id) filter ( where c.latest = c.date )                    as contributors_count,
       count(distinct c.contributor_id) filter ( where c.first = c.date )                     as new_contributors_count,
       count(distinct c.contributor_id) filter ( where c.latest = c.date - interval '1 week') as churned_contributors_count
FROM bi.weekly_contributors c
         join project_github_repos pgr on pgr.github_repo_id = c.repo_id
         join projects_ecosystems pe on pe.project_id = pgr.project_id
         join ecosystems e on e.id = pe.ecosystem_id
group by e.name, c.date
;

create view bi.weekly_contributions_stats_per_sponsor as
SELECT s.name                                                                                 as sponsor,
       c.date                                                                                 as creation_date,
       count(distinct c.contributor_id) filter ( where c.latest = c.date )                    as contributors_count,
       count(distinct c.contributor_id) filter ( where c.first = c.date )                     as new_contributors_count,
       count(distinct c.contributor_id) filter ( where c.latest = c.date - interval '1 week') as churned_contributors_count
FROM bi.weekly_contributors c
         join project_github_repos pgr on pgr.github_repo_id = c.repo_id
         join projects_sponsors ps on ps.project_id = pgr.project_id
         join sponsors s on s.id = ps.sponsor_id
group by s.name, c.date
;



CREATE VIEW bi.weekly_rewards_creation_stats_per_currency_sponsor AS
SELECT DATE_TRUNC('week', r.requested_at) AS creation_date,
       s.name                             as sponsor,
       c.code                             AS currency,
       SUM(rsd.amount_usd_equivalent)     AS total_usd_amount,
       SUM(r.amount)                      AS total_amount,
       COUNT(DISTINCT r.id)               AS rewards_count,
       COUNT(DISTINCT r.recipient_id)     AS contributors_count,
       COUNT(DISTINCT r.requestor_id)     AS project_leads_count
FROM rewards r
         JOIN currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
         JOIN projects_sponsors ps on ps.project_id = r.project_id
         JOIN sponsors s on s.id = ps.sponsor_id
GROUP BY creation_date, s.name, c.code;


CREATE VIEW bi.monthly_rewards_creation_stats_per_currency_sponsor AS
SELECT DATE_TRUNC('month', r.requested_at) AS creation_date,
       c.code                              AS currency,
       s.name                              as sponsor,
       SUM(rsd.amount_usd_equivalent)      AS total_usd_amount,
       SUM(r.amount)                       AS total_amount,
       COUNT(DISTINCT r.id)                AS rewards_count,
       COUNT(DISTINCT r.recipient_id)      AS contributors_count,
       COUNT(DISTINCT r.requestor_id)      AS project_leads_count
FROM rewards r
         JOIN currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
         JOIN projects_sponsors ps on ps.project_id = r.project_id
         JOIN sponsors s on s.id = ps.sponsor_id
GROUP BY creation_date, s.name, c.code;



CREATE VIEW bi.weekly_rewards_creation_stats_per_sponsor AS
SELECT DATE_TRUNC('week', r.requested_at) AS creation_date,
       s.name                             as sponsor,
       SUM(rsd.amount_usd_equivalent)     AS total_usd_amount,
       COUNT(DISTINCT r.id)               AS rewards_count,
       COUNT(DISTINCT r.recipient_id)     AS contributors_count,
       COUNT(DISTINCT r.requestor_id)     AS project_leads_count
FROM rewards r
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
         JOIN projects_sponsors ps on ps.project_id = r.project_id
         JOIN sponsors s on s.id = ps.sponsor_id
GROUP BY creation_date, s.name;


CREATE VIEW bi.monthly_rewards_creation_stats_per_sponsor AS
SELECT DATE_TRUNC('month', r.requested_at) AS creation_date,
       s.name                              as sponsor,
       SUM(rsd.amount_usd_equivalent)      AS total_usd_amount,
       COUNT(DISTINCT r.id)                AS rewards_count,
       COUNT(DISTINCT r.recipient_id)      AS contributors_count,
       COUNT(DISTINCT r.requestor_id)      AS project_leads_count
FROM rewards r
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
         JOIN projects_sponsors ps on ps.project_id = r.project_id
         JOIN sponsors s on s.id = ps.sponsor_id
GROUP BY creation_date, s.name;

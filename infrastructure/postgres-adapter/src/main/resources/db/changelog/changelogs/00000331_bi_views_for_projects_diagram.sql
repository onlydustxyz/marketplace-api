CREATE FUNCTION sum_func(numeric, anyelement, numeric)
    RETURNS numeric AS
$body$
SELECT case when $3 is not null then COALESCE($1, 0) + $3 else $1 end
$body$
    LANGUAGE 'sql';

CREATE FUNCTION sum_func(bigint, anyelement, bigint)
    RETURNS bigint AS
$body$
SELECT case when $3 is not null then COALESCE($1, 0) + $3 else $1 end
$body$
    LANGUAGE 'sql';

CREATE AGGREGATE dist_sum (pg_catalog."any", numeric)
    (
    SFUNC = sum_func,
    STYPE = numeric
    );

CREATE AGGREGATE dist_sum (pg_catalog."any", bigint)
    (
    SFUNC = sum_func,
    STYPE = bigint
    );



CREATE FUNCTION accounting.usd_quote_at(currency_id UUID, at timestamp with time zone)
    RETURNS NUMERIC AS
$$
SELECT price
FROM accounting.historical_quotes hq
         JOIN currencies usd ON usd.id = hq.target_id and usd.code = 'USD'
WHERE hq.base_id = currency_id
  AND hq.timestamp <= at
ORDER BY hq.timestamp DESC
LIMIT 1
$$ LANGUAGE SQL;



CREATE SCHEMA bi_internal;


CREATE OR REPLACE VIEW bi_internal.exploded_contributions AS
select c.id                            as contribution_id,
       c.contributor_id                as contributor_id,
       c.created_at                    as timestamp,
       projects.id                     as project_id,
       ppc.project_category_id         as project_category_id,
       lfe.language_id                 as language_id,
       pe.ecosystem_id                 as ecosystem_id,
       pp.program_id                   as program_id,
       c.type = 'PULL_REQUEST'         as is_merged_pr,
       c.created_at = first.created_at as is_first_contribution_on_onlydust
from completed_contributions c
         CROSS JOIN unnest(c.project_ids) AS projects(id)
         JOIN (SELECT cc.contributor_id, min(cc.created_at) AS created_at
               FROM completed_contributions cc
               GROUP BY cc.contributor_id) first
              ON first.contributor_id = c.contributor_id
         LEFT JOIN projects_ecosystems pe ON pe.project_id = projects.id
         LEFT JOIN programs_projects pp ON pp.project_id = projects.id
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = projects.id
         LEFT JOIN LATERAL ( SELECT DISTINCT lfe_1.language_id
                             FROM language_file_extensions lfe_1
                             WHERE lfe_1.extension = ANY (c.main_file_extensions)) lfe ON true;



CREATE VIEW bi_internal.exploded_rewards AS
SELECT r.id                      as reward_id,
       r.recipient_id            as contributor_id,
       r.requested_at            as timestamp,
       projects.id               as project_id,
       ppc.project_category_id   as project_category_id,
       lfe.language_id           as language_id,
       pe.ecosystem_id           as ecosystem_id,
       pp.program_id             as program_id,
       r.amount_usd_equivalent   as amount_usd,
       merged_prs.id IS NOT NULL as is_merged_pr
FROM completed_contributions c
         CROSS JOIN unnest(c.project_ids) AS projects(id)
         LEFT JOIN projects_ecosystems pe ON pe.project_id = projects.id
         LEFT JOIN programs_projects pp ON pp.project_id = projects.id
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = projects.id
         LEFT JOIN indexer_exp.github_pull_requests merged_prs ON merged_prs.id = c.pull_request_id
         LEFT JOIN LATERAL ( SELECT DISTINCT lfe_1.language_id
                             FROM language_file_extensions lfe_1
                             WHERE lfe_1.extension = ANY (c.main_file_extensions)) lfe ON true

         LEFT JOIN LATERAL ( SELECT r.id,
                                    r.recipient_id,
                                    r.requested_at,
                                    r.amount,
                                    rsd.amount_usd_equivalent
                             FROM rewards r
                                      JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                                      JOIN reward_items ri
                                           ON ri.reward_id = r.id
                                               AND ri.type = c.type::text::contribution_type
                                               AND ri.number = c.github_number
                                               AND r.recipient_id = c.contributor_id) r
                   ON true
;



CREATE VIEW bi_internal.daily_project_grants(project_id, program_id, day_timestamp, usd_amount) AS
select abt.project_id,
       abt.program_id,
       date_trunc('day', abt.timestamp) as day_timestamp,
       sum(abt.usd_amount)
from (SELECT abt.project_id,
             abt.program_id,
             CASE
                 WHEN abt.type = 'TRANSFER' THEN abt.amount * hq.usd_conversion_rate
                 ELSE abt.amount * hq.usd_conversion_rate * -1 END as usd_amount,
             abt.timestamp
      FROM accounting.account_book_transactions abt
               JOIN LATERAL (select accounting.usd_quote_at(abt.currency_id, abt.timestamp) as usd_conversion_rate) hq
                    ON true
      WHERE abt.project_id IS NOT NULL
        AND (abt.type = 'TRANSFER' OR abt.type = 'REFUND')
        AND abt.reward_id IS NULL
        AND abt.payment_id IS NULL) abt
group by abt.project_id, abt.program_id, date_trunc('day', abt.timestamp);



CREATE MATERIALIZED VIEW bi_internal.contribution_project_timestamps AS
SELECT DISTINCT c.created_at as timestamp,
                projects.id  as project_id
FROM completed_contributions c
         CROSS JOIN unnest(c.project_ids) AS projects(id)
;
create unique index contribution_project_timestamps_pk on bi_internal.contribution_project_timestamps (project_id, timestamp);
create unique index contribution_project_timestamps_pk_inv on bi_internal.contribution_project_timestamps (timestamp, project_id);
refresh materialized view bi_internal.contribution_project_timestamps;



CREATE MATERIALIZED VIEW bi.project_contribution_data AS
select c.project_id                                                              as project_id,
       c.timestamp                                                               as timestamp,
       array_agg(distinct c.language_id)                                         as language_ids,
       array_agg(distinct c.ecosystem_id)                                        as ecosystem_ids,
       array_agg(distinct c.program_id)                                          as program_ids,
       array_agg(distinct c.project_category_id)                                 as project_category_ids,
       count(distinct c.contribution_id)                                         as contribution_count,
       count(distinct c.contribution_id) filter ( where c.is_merged_pr is true ) as merged_pr_count,
       previous.timestamp                                                        as previous_contribution_timestamp,
       next.timestamp                                                            as next_contribution_timestamp
from bi_internal.exploded_contributions c
         left join lateral ( (select max(previous.timestamp) as timestamp
                              from bi_internal.contribution_project_timestamps previous
                              where previous.project_id = c.project_id
                                and previous.timestamp < c.timestamp) ) previous on true
         left join lateral ( (select min(next.timestamp) as timestamp
                              from bi_internal.contribution_project_timestamps next
                              where next.project_id = c.project_id
                                and next.timestamp > c.timestamp) ) next on true
where c.project_id is not null
  and c.timestamp is not null
group by c.project_id, c.timestamp, previous.timestamp, next.timestamp;

create unique index project_contribution_data_pk on bi.project_contribution_data (project_id, timestamp);
create unique index project_contribution_data_pk_inv on bi.project_contribution_data (timestamp, project_id);
refresh materialized view bi.project_contribution_data;



CREATE MATERIALIZED VIEW bi.project_reward_data AS
select r.project_id                                 as project_id,
       r.timestamp                                  as timestamp,
       array_agg(distinct r.language_id)            as language_ids,
       array_agg(distinct r.ecosystem_id)           as ecosystem_ids,
       array_agg(distinct r.program_id)             as program_ids,
       array_agg(distinct r.project_category_id)    as project_category_ids,
       count(distinct r.reward_id)                  as reward_count,
       dist_sum(distinct r.reward_id, r.amount_usd) as total_rewarded_usd,
       avg(r.amount_usd)                            as avg_rewarded_usd
from bi_internal.exploded_rewards r
where r.project_id is not null
  and r.timestamp is not null
group by r.project_id, r.timestamp;

create unique index project_ungrouped_reward_data_pk on bi.project_reward_data (project_id, timestamp);
create unique index project_ungrouped_reward_data_pk_inv on bi.project_reward_data (timestamp, project_id);
refresh materialized view bi.project_reward_data;



CREATE MATERIALIZED VIEW bi.project_daily_grant_data AS
select pg.project_id      as project_id,
       pg.day_timestamp   as timestamp,
       sum(pg.usd_amount) as total_granted_usd
from bi_internal.daily_project_grants pg
where pg.project_id is not null
  and pg.day_timestamp is not null
group by pg.project_id, pg.day_timestamp;

create unique index project_daily_grant_data_pk on bi.project_daily_grant_data (project_id, timestamp);
create unique index project_daily_grant_data_pk_inv on bi.project_daily_grant_data (timestamp, project_id);
refresh materialized view bi.project_daily_grant_data;
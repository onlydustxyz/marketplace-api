DROP MATERIALIZED VIEW IF EXISTS poc.contributions;

CREATE MATERIALIZED VIEW poc.contributions AS
SELECT c.id                      as contribution_id,
       c.contributor_id          as contributor_id,
       c.created_at              as timestamp,
       projects.id               as project_id,
       ppc.project_category_id   as project_category_id,
       lfe.language_id           as language_id,
       pe.ecosystem_id           as ecosystem_id,
       merged_prs.id IS NOT NULL as is_merged_pr
FROM public_contributions c
         LEFT JOIN projects_ecosystems pe ON pe.project_id = ANY (c.project_ids)

         CROSS JOIN unnest(c.project_ids) AS projects(id)
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = projects.id

         LEFT JOIN indexer_exp.github_pull_requests merged_prs
                   ON merged_prs.id = c.pull_request_id AND c.status = 'COMPLETED'

         LEFT JOIN LATERAL ( SELECT DISTINCT lfe_1.language_id
                             FROM language_file_extensions lfe_1
                             WHERE lfe_1.extension = ANY (c.main_file_extensions)) lfe ON true
;

create index on poc.contributions (contributor_id);
create index on poc.contributions (project_id);
create index on poc.contributions (timestamp);


DROP MATERIALIZED VIEW IF EXISTS poc.rewards;

CREATE MATERIALIZED VIEW poc.rewards AS
SELECT r.id                      as reward_id,
       r.recipient_id            as contributor_id,
       r.requested_at            as timestamp,
       projects.id               as project_id,
       ppc.project_category_id   as project_category_id,
       lfe.language_id           as language_id,
       pe.ecosystem_id           as ecosystem_id,
       r.amount_usd_equivalent   as amount_usd,
       merged_prs.id IS NOT NULL as is_merged_pr
FROM public_contributions c
         LEFT JOIN projects_ecosystems pe ON pe.project_id = ANY (c.project_ids)

         CROSS JOIN unnest(c.project_ids) AS projects(id)
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = projects.id

         LEFT JOIN indexer_exp.github_pull_requests merged_prs
                   ON merged_prs.id = c.pull_request_id AND c.status = 'COMPLETED'

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
                                      JOIN reward_items ri ON ri.reward_id = r.id
                                 AND ri.type = c.type::text::contribution_type AND ri.number = c.github_number AND
                                                              r.recipient_id = c.contributor_id) r
                   ON true
;

create index on poc.rewards (contributor_id);
create index on poc.rewards (project_id);
create index on poc.rewards (timestamp);


select c.contributor_id                                                            as contributor_id,
       date_trunc('day', c.timestamp)                                              as week,
       array_agg(distinct c.project_id)                                            as projects,
       array_agg(distinct c.project_category_id)                                   as categories,
       array_agg(distinct c.language_id)                                           as languages,
       array_agg(distinct c.ecosystem_id)                                          as ecosystems,
       array_agg(distinct c.contribution_id)                                       as contributions,
       array_agg(distinct contribution_id) filter ( where c.is_merged_pr is true ) as merged_prs
from poc.contributions c
group by c.contributor_id,
         date_trunc('day', c.timestamp)
;


DROP FUNCTION sum_func(
    double precision, pg_catalog.anyelement, double precision
);
CREATE OR REPLACE FUNCTION sum_func(
    numeric, pg_catalog.anyelement, numeric
)
    RETURNS numeric AS
$body$
SELECT case when $3 is not null then COALESCE($1, 0) + $3 else $1 end
$body$
    LANGUAGE 'sql';

DROP AGGREGATE dist_sum(
    pg_catalog."any",
    double precision);
CREATE AGGREGATE dist_sum (
    pg_catalog."any",
    numeric)
    (
    SFUNC = sum_func,
    STYPE = numeric
    );



create table poc.foo
(
    id     int,
    amount int
);
truncate table poc.foo;
insert into poc.foo
values (1, 20),
       (1, 10),
       (2, 30),
       (2, 30),
       (3, 50),
       (3, 50);

select sum(amount), dist_sum(distinct id, amount)
from poc.foo;



drop MATERIALIZED view poc.project_grants;

create MATERIALIZED view poc.project_grants(project_id, program_id, day_timestamp, usd_amount) as

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
               JOIN LATERAL (select hq.price as usd_conversion_rate
                             from accounting.historical_quotes hq
                             WHERE hq.base_id = abt.currency_id
                               AND hq.target_id = (select id from currencies where code = 'USD')
                               AND hq.timestamp <= abt.timestamp
                             ORDER BY hq.timestamp DESC
                             LIMIT 1) hq
                    ON true
      WHERE abt.project_id IS NOT NULL
        AND (abt.type = 'TRANSFER' OR abt.type = 'REFUND')
        AND abt.reward_id IS NULL
        AND abt.payment_id IS NULL) abt
group by abt.project_id, abt.program_id, date_trunc('day', abt.timestamp);


create index on poc.project_grants (program_id);
create index on poc.project_grants (project_id);
create index on poc.project_grants (day_timestamp);


drop MATERIALIZED view poc.project_contributors;
create MATERIALIZED view poc.project_contributors as
select c.contributor_id   as contributor_id,
       projects.id        as project_id,
       c.created_at       as timestamp,
       case
           when c.created_at = first_contribution.first_created_at then true
           else false end as is_first_contribution_on_onlydust
from public_contributions c
         join project_github_repos pgr on pgr.github_repo_id = c.repo_id
         CROSS JOIN unnest(c.project_ids) AS projects(id)
         join (select cc.contributor_id, min(cc.created_at) as first_created_at
               from public_contributions cc
               group by cc.contributor_id) first_contribution
              ON first_contribution.contributor_id = c.contributor_id
where c.status = 'COMPLETED'
  and c.type = 'PULL_REQUEST';



create index on poc.project_contributors (project_id);
create index on poc.project_contributors (contributor_id);
create index on poc.project_contributors (timestamp);


drop materialized view poc.project_ungrouped_data;
create materialized view poc.project_ungrouped_data as
select c.project_id                                                            as project_id,
       c.timestamp                                                             as timestamp,
       NULL::numeric                                                           as total_granted_usd,
       count(distinct c.contribution_id)                                       as contribution_count,
       NULL::bigint                                                            as reward_count,
       NULL::numeric                                                           as total_rewarded_usd,
       NULL::numeric                                                           as avg_rewarded_usd,
       count(distinct contribution_id) filter ( where c.is_merged_pr is true ) as merged_pr_count,
       NULL::bigint                                                            as active_contributor_count,
       NULL::bigint                                                            as onboarded_contributor_count
from poc.contributions c
group by c.project_id, c.timestamp

union

select r.project_id                                 as project_id,
       r.timestamp                                  as timestamp,
       NULL::numeric                                as total_granted_usd,
       NULL                                         as contribution_count,
       count(distinct r.reward_id)                  as reward_count,
       dist_sum(distinct r.reward_id, r.amount_usd) as total_rewarded_usd,
       avg(r.amount_usd)                            as avg_rewarded_usd,
       NULL                                         as merged_pr_count,
       NULL                                         as active_contributor_count,
       NULL                                         as onboarded_contributor_count
from poc.rewards r
group by r.project_id, r.timestamp

union

select pg.project_id      as project_id,
       pg.day_timestamp   as timestamp,
       sum(pg.usd_amount) as total_granted_usd,
       NULL               as contribution_count,
       NULL               as reward_count,
       NULL               as total_rewarded_usd,
       NULL               as avg_rewarded_usd,
       NULL               as merged_pr_count,
       NULL               as active_contributor_count,
       NULL               as onboarded_contributor_count
from poc.project_grants pg
group by pg.project_id, pg.day_timestamp

union

select pc.project_id                                                 as project_id,
       pc.timestamp                                                  as timestamp,
       NULL                                                          as total_granted_usd,
       NULL                                                          as contribution_count,
       NULL                                                          as reward_count,
       NULL                                                          as total_rewarded_usd,
       NULL                                                          as avg_rewarded_usd,
       NULL                                                          as merged_pr_count,
       count(distinct pc.contributor_id)                             as active_contributor_count,
       count(distinct pc.contributor_id)
       filter ( where pc.is_first_contribution_on_onlydust is true ) as onboarded_contributor_count
from poc.project_contributors pc
group by pc.project_id, pc.timestamp;

create index on poc.project_ungrouped_data (project_id);
create index on poc.project_ungrouped_data (timestamp);



SELECT ungrouped.project_id,
       sum(ungrouped.total_granted_usd) - sum(ungrouped.total_rewarded_usd)              as available_budget,
       sum(ungrouped.total_granted_usd) / greatest(sum(ungrouped.total_rewarded_usd), 1) as percent_budget_utilized,
       sum(ungrouped.total_granted_usd)                                                  as total_granted_usd,
       sum(ungrouped.contribution_count)                                                 as contribution_count,
       sum(ungrouped.reward_count)                                                       as reward_count,
       sum(ungrouped.total_rewarded_usd)                                                 as total_rewarded_usd,
       avg(ungrouped.avg_rewarded_usd)                                                   as avg_rewarded_usd,
       sum(ungrouped.merged_pr_count)                                                    as merged_pr_count,
       sum(ungrouped.active_contributor_count)                                           as active_contributor_count,
       sum(ungrouped.onboarded_contributor_count)                                        as onboarded_contributor_count
from poc.project_ungrouped_data ungrouped
where ungrouped.timestamp > '2020-01-01'
  and ungrouped.timestamp < '2025-01-02'
group by ungrouped.project_id
offset 100 limit 20;


SELECT ungrouped.project_id,
       date_trunc('week', ungrouped.timestamp)                                           as week,
       sum(ungrouped.total_granted_usd) - sum(ungrouped.total_rewarded_usd)              as available_budget,
       sum(ungrouped.total_granted_usd) / greatest(sum(ungrouped.total_rewarded_usd), 1) as percent_budget_utilized,
       sum(ungrouped.total_granted_usd)                                                  as total_granted_usd,
       sum(ungrouped.contribution_count)                                                 as contribution_count,
       sum(ungrouped.reward_count)                                                       as reward_count,
       sum(ungrouped.total_rewarded_usd)                                                 as total_rewarded_usd,
       avg(ungrouped.avg_rewarded_usd)                                                   as avg_rewarded_usd,
       sum(ungrouped.merged_pr_count)                                                    as merged_pr_count,
       sum(ungrouped.active_contributor_count)                                           as active_contributor_count,
       sum(ungrouped.onboarded_contributor_count)                                        as onboarded_contributor_count
from poc.project_ungrouped_data ungrouped
where ungrouped.timestamp > '2020-01-01'
  and ungrouped.timestamp < '2025-01-02'
group by ungrouped.project_id, date_trunc('week', ungrouped.timestamp);



select rsd.amount_usd_equivalent
from rewards r
         join accounting.reward_status_data rsd on rsd.reward_id = r.id
where r.project_id = 'b4b66d1c-7d6d-41f8-8ead-c35ef570824e'
  and date_trunc('week', r.requested_at) = '2024-04-15';



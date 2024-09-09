CREATE FUNCTION sum_func(numeric, pg_catalog.anyelement, numeric)
    RETURNS numeric AS
$body$
SELECT case when $3 is not null then COALESCE($1, 0) + $3 else $1 end
$body$
    LANGUAGE 'sql';


CREATE AGGREGATE dist_sum (pg_catalog."any", numeric)
    (
    SFUNC = sum_func,
    STYPE = numeric
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


drop materialized VIEW bi.exploded_contributions;
CREATE VIEW bi.exploded_contributions AS
SELECT c.id                      as contribution_id,
       c.contributor_id          as contributor_id,
       c.created_at              as timestamp,
       projects.id               as project_id,
       ppc.project_category_id   as project_category_id,
       lfe.language_id           as language_id,
       pe.ecosystem_id           as ecosystem_id,
       pp.program_id             as program_id,
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
;



CREATE VIEW bi.exploded_rewards AS
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



create view bi.daily_project_grants(project_id, program_id, day_timestamp, usd_amount) as
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



create view bi.exploded_project_contributors as
select c.contributor_id        as contributor_id,
       projects.id             as project_id,
       c.created_at            as timestamp,
       case
           when c.created_at = first_contribution.first_created_at then true
           else false end      as is_first_contribution_on_onlydust,
       ppc.project_category_id as project_category_id,
       lfe.language_id         as language_id,
       pe.ecosystem_id         as ecosystem_id,
       pp.program_id           as program_id
from completed_contributions c
         join project_github_repos pgr on pgr.github_repo_id = c.repo_id
         CROSS JOIN unnest(c.project_ids) AS projects(id)
         join (select cc.contributor_id, min(cc.created_at) as first_created_at
               from completed_contributions cc
               group by cc.contributor_id) first_contribution
              ON first_contribution.contributor_id = c.contributor_id
         LEFT JOIN projects_ecosystems pe ON pe.project_id = projects.id
         LEFT JOIN programs_projects pp ON pp.project_id = projects.id
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = projects.id
         LEFT JOIN LATERAL ( SELECT DISTINCT lfe_1.language_id
                             FROM language_file_extensions lfe_1
                             WHERE lfe_1.extension = ANY (c.main_file_extensions)) lfe ON true
where c.status = 'COMPLETED'
  and c.type = 'PULL_REQUEST';



CREATE MATERIALIZED VIEW bi.contribution_project_timestamps AS
SELECT DISTINCT c.created_at as timestamp,
                projects.id  as project_id
FROM completed_contributions c
         CROSS JOIN unnest(c.project_ids) AS projects(id)
;
create unique index on bi.contribution_project_timestamps (project_id, timestamp);
create unique index on bi.contribution_project_timestamps (timestamp, project_id);


drop materialized view bi.project_ungrouped_contribution_data;
create materialized view bi.project_ungrouped_contribution_data as
select c.project_id                                                            as project_id,
       c.timestamp                                                             as timestamp,
       array_agg(distinct c.language_id)                                       as language_ids,
       array_agg(distinct c.ecosystem_id)                                      as ecosystem_ids,
       array_agg(distinct c.program_id)                                        as program_ids,
       array_agg(distinct c.project_category_id)                               as project_category_ids,
       count(distinct c.contribution_id)                                       as contribution_count,
       count(distinct contribution_id) filter ( where c.is_merged_pr is true ) as merged_pr_count,
       previous.timestamp                                                      as previous_contribution_timestamp,
       next.timestamp                                                          as next_contribution_timestamp
from bi.exploded_contributions c
         left join lateral ( (select max(previous.timestamp) as timestamp
                              from bi.contribution_project_timestamps previous
                              where previous.project_id = c.project_id
                                and previous.timestamp < c.timestamp) ) previous on true
         left join lateral ( (select min(next.timestamp) as timestamp
                              from bi.contribution_project_timestamps next
                              where next.project_id = c.project_id
                                and next.timestamp > c.timestamp) ) next on true
where c.project_id is not null
  and c.timestamp is not null
group by c.project_id, c.timestamp, previous.timestamp, next.timestamp;

create unique index on bi.project_ungrouped_contribution_data (project_id, timestamp);
create unique index on bi.project_ungrouped_contribution_data (timestamp, project_id);



SELECT date_trunc('week', ungrouped.timestamp)                                                    as period,
       count(distinct ungrouped.project_id)                                                       as active_project_count,

       count(distinct ungrouped.project_id)
       filter (where ungrouped.previous_contribution_timestamp is null)                           as new_project_count,

       count(distinct ungrouped.project_id)
       filter (where ungrouped.previous_contribution_timestamp < date_trunc('week', ungrouped.timestamp) -
                                                                 interval '1 week')               as reactivated_project_count,

       count(distinct ungrouped.project_id)
       filter (where ungrouped.next_contribution_timestamp is null and date_trunc('week', ungrouped.timestamp) <
                                                                       date_trunc('week', now())) as next_period_churned_project_count
from bi.project_ungrouped_contribution_data ungrouped
where ungrouped.timestamp > '2020-01-01'
  and ungrouped.timestamp < '2025-01-02'
  and (('{63a96d57-86ef-4b21-ab46-25e9518a5d9d,
de948c03-f39f-4342-a652-2523f1c15abd,
1deee90d-39ba-4adb-aebc-19bae9bf4edd,
57b061e1-bc67-49ee-9f93-53966bf31438,
de4c23b6-c2bd-48c2-99e4-4f1c5ecf0821,
d0da4d3b-3369-4f0f-aaac-26e78feb71ab,
5f98d8b8-7dfa-4fc9-8ac9-e6454ae1653a,
5f4726f2-0990-4421-aedf-3bdd6e69c0a0,
c493b842-0db8-435b-85c7-5fe63ffe81f5,
70f46521-7284-49dd-bd5b-1e5c23178ad4}'::uuid[] && ungrouped.program_ids)
    or ('{63a96d57-86ef-4b21-ab46-25e9518a5d9d,
de948c03-f39f-4342-a652-2523f1c15abd,
1deee90d-39ba-4adb-aebc-19bae9bf4edd,
57b061e1-bc67-49ee-9f93-53966bf31438,
de4c23b6-c2bd-48c2-99e4-4f1c5ecf0821,
d0da4d3b-3369-4f0f-aaac-26e78feb71ab,
5f98d8b8-7dfa-4fc9-8ac9-e6454ae1653a,
5f4726f2-0990-4421-aedf-3bdd6e69c0a0,
c493b842-0db8-435b-85c7-5fe63ffe81f5,
70f46521-7284-49dd-bd5b-1e5c23178ad4}'::uuid[] && ungrouped.ecosystem_ids))
group by date_trunc('week', ungrouped.timestamp)
order by 1 desc nulls last
offset 0 limit 100;



drop materialized view bi.project_ungrouped_data;
create materialized view bi.project_ungrouped_data as
select c.project_id                                                            as project_id,
       c.timestamp                                                             as timestamp,
       array_agg(distinct c.language_id)                                       as language_ids,
       array_agg(distinct c.ecosystem_id)                                      as ecosystem_ids,
       array_agg(distinct c.program_id)                                        as program_ids,
       array_agg(distinct c.project_category_id)                               as project_category_ids,
       NULL::numeric                                                           as total_granted_usd,
       count(distinct c.contribution_id)                                       as contribution_count,
       NULL::bigint                                                            as reward_count,
       NULL::numeric                                                           as total_rewarded_usd,
       NULL::numeric                                                           as avg_rewarded_usd,
       count(distinct contribution_id) filter ( where c.is_merged_pr is true ) as merged_pr_count,
       NULL::bigint                                                            as active_contributor_count,
       NULL::bigint                                                            as onboarded_contributor_count
from bi.exploded_contributions c
where c.project_id is not null
  and c.timestamp is not null
group by c.project_id, c.timestamp

union

select r.project_id                                 as project_id,
       r.timestamp                                  as timestamp,
       array_agg(distinct r.language_id)            as language_ids,
       array_agg(distinct r.ecosystem_id)           as ecosystem_ids,
       array_agg(distinct r.program_id)             as program_ids,
       array_agg(distinct r.project_category_id)    as project_category_ids,
       NULL::numeric                                as total_granted_usd,
       NULL                                         as contribution_count,
       count(distinct r.reward_id)                  as reward_count,
       dist_sum(distinct r.reward_id, r.amount_usd) as total_rewarded_usd,
       avg(r.amount_usd)                            as avg_rewarded_usd,
       NULL                                         as merged_pr_count,
       NULL                                         as active_contributor_count,
       NULL                                         as onboarded_contributor_count
from bi.exploded_rewards r
where r.project_id is not null
  and r.timestamp is not null
group by r.project_id, r.timestamp

union

select pg.project_id      as project_id,
       pg.day_timestamp   as timestamp,
       NULL               as language_ids,
       NULL               as ecosystem_ids,
       NULL               as program_ids,
       NULL               as project_category_ids,
       sum(pg.usd_amount) as total_granted_usd,
       NULL               as contribution_count,
       NULL               as reward_count,
       NULL               as total_rewarded_usd,
       NULL               as avg_rewarded_usd,
       NULL               as merged_pr_count,
       NULL               as active_contributor_count,
       NULL               as onboarded_contributor_count
from bi.daily_project_grants pg
where pg.project_id is not null
  and pg.day_timestamp is not null
group by pg.project_id, pg.day_timestamp

union

select pc.project_id                                                 as project_id,
       pc.timestamp                                                  as timestamp,
       array_agg(distinct pc.language_id)                            as language_ids,
       array_agg(distinct pc.ecosystem_id)                           as ecosystem_ids,
       array_agg(distinct pc.program_id)                             as program_ids,
       array_agg(distinct pc.project_category_id)                    as project_category_ids,
       NULL                                                          as total_granted_usd,
       NULL                                                          as contribution_count,
       NULL                                                          as reward_count,
       NULL                                                          as total_rewarded_usd,
       NULL                                                          as avg_rewarded_usd,
       NULL                                                          as merged_pr_count,
       count(distinct pc.contributor_id)                             as active_contributor_count,
       count(distinct pc.contributor_id)
       filter ( where pc.is_first_contribution_on_onlydust is true ) as onboarded_contributor_count
from bi.exploded_project_contributors pc
where pc.project_id is not null
  and pc.timestamp is not null
group by pc.project_id, pc.timestamp;

create index on bi.project_ungrouped_data (project_id);
create index on bi.project_ungrouped_data (timestamp);



SELECT ungrouped.project_id,
       sum(ungrouped.total_granted_usd) - sum(ungrouped.total_rewarded_usd) as available_budget,
       sum(ungrouped.total_granted_usd) /
       greatest(sum(ungrouped.total_rewarded_usd), 1)                       as percent_budget_utilized,
       sum(ungrouped.total_granted_usd)                                     as total_granted_usd,
       sum(ungrouped.contribution_count)                                    as contribution_count,
       sum(ungrouped.reward_count)                                          as reward_count,
       sum(ungrouped.total_rewarded_usd)                                    as total_rewarded_usd,
       avg(ungrouped.avg_rewarded_usd)                                      as avg_rewarded_usd,
       sum(ungrouped.merged_pr_count)                                       as merged_pr_count,
       sum(ungrouped.active_contributor_count)                              as active_contributor_count,
       sum(ungrouped.onboarded_contributor_count)                           as onboarded_contributor_count,
       array_agg(distinct ungrouped.language_ids[1])
       filter ( where ungrouped.language_ids[1] is not null )               as language_ids
from bi.project_ungrouped_data ungrouped
where ungrouped.timestamp > '2020-01-01'
  and ungrouped.timestamp < '2025-01-02'
  and ('be7711c1-4373-4864-b503-78ed84af8d3d' = any (ungrouped.language_ids) or ungrouped.language_ids is null)
group by ungrouped.project_id
order by sum(ungrouped.reward_count) desc nulls last
offset 0 limit 1;


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
from bi.project_ungrouped_data ungrouped
where ungrouped.timestamp > '2020-01-01'
  and ungrouped.timestamp < '2025-01-02'
group by ungrouped.project_id, date_trunc('week', ungrouped.timestamp);



drop materialized view bi.contributor_ungrouped_data;
create materialized view bi.contributor_ungrouped_data as
select c.contributor_id                                                        as contributor_id,
       c.timestamp                                                             as timestamp,
       NULL::numeric                                                           as total_granted_usd,
       count(distinct c.contribution_id)                                       as contribution_count,
       NULL::bigint                                                            as reward_count,
       NULL::numeric                                                           as total_rewarded_usd,
       NULL::numeric                                                           as avg_rewarded_usd,
       count(distinct contribution_id) filter ( where c.is_merged_pr is true ) as merged_pr_count,
       NULL::bigint                                                            as active_contributor_count,
       NULL::bigint                                                            as onboarded_contributor_count
from bi.contributions c
group by c.contributor_id, c.timestamp

union

select r.contributor_id                             as contributor_id,
       r.timestamp                                  as timestamp,
       NULL::numeric                                as total_granted_usd,
       NULL                                         as contribution_count,
       count(distinct r.reward_id)                  as reward_count,
       dist_sum(distinct r.reward_id, r.amount_usd) as total_rewarded_usd,
       avg(r.amount_usd)                            as avg_rewarded_usd,
       NULL                                         as merged_pr_count,
       NULL                                         as active_contributor_count,
       NULL                                         as onboarded_contributor_count
from bi.rewards r
group by r.contributor_id, r.timestamp;


create index on bi.contributor_ungrouped_data (contributor_id);
create index on bi.contributor_ungrouped_data (timestamp);



SELECT ungrouped.contributor_id,
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
from bi.contributor_ungrouped_data ungrouped
where ungrouped.timestamp > '2020-01-01'
  and ungrouped.timestamp < '2025-01-02'
group by ungrouped.contributor_id
offset 100 limit 20;


SELECT ungrouped.contributor_id,
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
from bi.contributor_ungrouped_data ungrouped
where ungrouped.timestamp > '2020-01-01'
  and ungrouped.timestamp < '2025-01-02'
group by ungrouped.contributor_id, date_trunc('week', ungrouped.timestamp);



select rsd.amount_usd_equivalent
from rewards r
         join accounting.reward_status_data rsd on rsd.reward_id = r.id
where r.project_id = 'b4b66d1c-7d6d-41f8-8ead-c35ef570824e'
  and date_trunc('week', r.requested_at) = '2024-04-15';



select sum(ungrouped.reward_count)
from bi.project_ungrouped_data ungrouped
where ungrouped.timestamp > '2020-01-01'
  and ungrouped.timestamp < '2025-01-02'
group by ungrouped.project_id;
select count(*)
from rewards;
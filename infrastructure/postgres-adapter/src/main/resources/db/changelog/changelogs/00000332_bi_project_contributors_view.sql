create schema if not exists bi_internal;

CREATE OR REPLACE VIEW bi_internal.exploded_project_contributors AS
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
                             WHERE lfe_1.extension = ANY (c.main_file_extensions)) lfe ON true;

create materialized view bi.contributor_data as
select pc.contributor_id                          as contributor_id,
       pc.timestamp                               as timestamp,
       array_agg(distinct pc.language_id)         as language_ids,
       array_agg(distinct pc.ecosystem_id)        as ecosystem_ids,
       array_agg(distinct pc.program_id)          as program_ids,
       array_agg(distinct pc.project_id)          as project_ids,
       array_agg(distinct pc.project_category_id) as project_category_ids
from bi_internal.exploded_project_contributors pc
where pc.contributor_id is not null
  and pc.timestamp is not null
group by pc.contributor_id, pc.timestamp;

create unique index bi_contributor_data_contributor_id_timestamp on bi.contributor_data (contributor_id, timestamp);


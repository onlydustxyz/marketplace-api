drop materialized view bi.contributor_data;

drop view bi_internal.exploded_project_contributors;

create view bi_internal.exploded_contributions as
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
         cross join unnest(c.project_ids) as projects(id)
         join (select cc.contributor_id, min(cc.created_at) as created_at
               from completed_contributions cc
               group by cc.contributor_id) first
              on first.contributor_id = c.contributor_id
         left join projects_ecosystems pe on pe.project_id = projects.id
         left join programs_projects pp on pp.project_id = projects.id
         left join projects_project_categories ppc on ppc.project_id = projects.id
         left join lateral ( select distinct lfe_1.language_id
                             from language_file_extensions lfe_1
                             where lfe_1.extension = any (c.main_file_extensions)) lfe on true;

create materialized view bi.contribution_data as
with registered_users as (select u.id             as id,
                                 u.github_user_id as github_user_id,
                                 kyc.country      as country
                          from iam.users u
                                   join accounting.billing_profiles_users bpu on bpu.user_id = u.id
                                   join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id)
select c.contribution_id                                                 as contribution_id,
       c.contributor_id                                                  as contributor_id,
       ru.id                                                             as contributor_user_id,
       ru.country                                                        as contributor_country,
       c.timestamp                                                       as timestamp,
       c.is_first_contribution_on_onlydust                               as is_first_contribution_on_onlydust,
       array_agg(distinct c.language_id)                                 as language_ids,
       array_agg(distinct c.ecosystem_id)                                as ecosystem_ids,
       array_agg(distinct c.program_id)                                  as program_ids,
       array_agg(distinct c.project_id)                                  as project_ids,
       array_agg(distinct c.project_category_id)                         as project_category_ids,
       count(distinct c.contribution_id) filter ( where c.is_merged_pr ) as merged_pr_count
from bi_internal.exploded_contributions c
         left join registered_users ru on ru.github_user_id = c.contributor_id
group by c.contribution_id,
         c.contributor_id,
         c.timestamp,
         c.is_first_contribution_on_onlydust,
         ru.id,
         ru.country;

create unique index bi_contribution_data_pk on bi.contribution_data (contribution_id);


create type application_status as enum ('PENDING', 'ACCEPTED', 'SHELVED');


-- CAUTION depends on p_contribution_data and p_contribution_contributors_data
call create_pseudo_projection('bi', 'application_data', $$
select a.id                                       as application_id,
       cd.contribution_uuid                       as contribution_uuid,
       a.received_at                              as timestamp,
       date_trunc('day', a.received_at)           as day_timestamp,
       date_trunc('week', a.received_at)          as week_timestamp,
       date_trunc('month', a.received_at)         as month_timestamp,
       date_trunc('quarter', a.received_at)       as quarter_timestamp,
       date_trunc('year', a.received_at)          as year_timestamp,
       a.applicant_id                             as contributor_id,
       a.origin                                   as origin,
       case
           when array [a.applicant_id] <@ ccd.assignee_ids then 'ACCEPTED'::application_status
           when array_length(ccd.assignee_ids, 1) > 0 then 'SHELVED'::application_status
           else 'PENDING'::application_status end as status,
       cd.project_id                              as project_id,
       cd.project_slug                            as project_slug,
       cd.repo_id                                 as repo_id,
       cd.ecosystem_ids                           as ecosystem_ids,
       cd.program_ids                             as program_ids,
       cd.language_ids                            as language_ids,
       cd.project_category_ids                    as project_category_ids,
       concat(cd.search, ' ', iu.login)           as search
from applications a
         join bi.p_contribution_data cd on cd.issue_id = a.issue_id
         join bi.p_contribution_contributors_data ccd on ccd.contribution_uuid = cd.contribution_uuid
         left join iam.all_indexed_users iu on iu.github_user_id = a.applicant_id
group by a.id, cd.contribution_uuid, ccd.contribution_uuid, iu.login
    $$, 'application_id');

create index on bi.p_application_data (contribution_uuid);
create index on bi.p_application_data (repo_id);

create index bi_p_application_data_project_id_timestamp_idx on bi.p_application_data (project_id, timestamp);
create index bi_p_application_data_project_id_day_timestamp_idx on bi.p_application_data (project_id, day_timestamp);
create index bi_p_application_data_project_id_week_timestamp_idx on bi.p_application_data (project_id, week_timestamp);
create index bi_p_application_data_project_id_month_timestamp_idx on bi.p_application_data (project_id, month_timestamp);
create index bi_p_application_data_project_id_quarter_timestamp_idx on bi.p_application_data (project_id, quarter_timestamp);
create index bi_p_application_data_project_id_year_timestamp_idx on bi.p_application_data (project_id, year_timestamp);
create index bi_p_application_data_project_id_timestamp_idx_inv on bi.p_application_data (timestamp, project_id);
create index bi_p_application_data_project_id_day_timestamp_idx_inv on bi.p_application_data (day_timestamp, project_id);
create index bi_p_application_data_project_id_week_timestamp_idx_inv on bi.p_application_data (week_timestamp, project_id);
create index bi_p_application_data_project_id_month_timestamp_idx_inv on bi.p_application_data (month_timestamp, project_id);
create index bi_p_application_data_project_id_quarter_timestamp_idx_inv on bi.p_application_data (quarter_timestamp, project_id);
create index bi_p_application_data_project_id_year_timestamp_idx_inv on bi.p_application_data (year_timestamp, project_id);

create index bi_p_application_data_contributor_id_timestamp_idx on bi.p_application_data (contributor_id, timestamp);
create index bi_p_application_data_contributor_id_day_timestamp_idx on bi.p_application_data (contributor_id, day_timestamp);
create index bi_p_application_data_contributor_id_week_timestamp_idx on bi.p_application_data (contributor_id, week_timestamp);
create index bi_p_application_data_contributor_id_month_timestamp_idx on bi.p_application_data (contributor_id, month_timestamp);
create index bi_p_application_data_contributor_id_quarter_timestamp_idx on bi.p_application_data (contributor_id, quarter_timestamp);
create index bi_p_application_data_contributor_id_year_timestamp_idx on bi.p_application_data (contributor_id, year_timestamp);
create index bi_p_application_data_contributor_id_timestamp_idx_inv on bi.p_application_data (timestamp, contributor_id);
create index bi_p_application_data_contributor_id_day_timestamp_idx_inv on bi.p_application_data (day_timestamp, contributor_id);
create index bi_p_application_data_contributor_id_week_timestamp_idx_inv on bi.p_application_data (week_timestamp, contributor_id);
create index bi_p_application_data_contributor_id_month_timestamp_idx_inv on bi.p_application_data (month_timestamp, contributor_id);
create index bi_p_application_data_contributor_id_quarter_timestamp_idx_inv on bi.p_application_data (quarter_timestamp, contributor_id);
create index bi_p_application_data_contributor_id_year_timestamp_idx_inv on bi.p_application_data (year_timestamp, contributor_id);



CREATE OR REPLACE FUNCTION bi.select_contributors(fromDate timestamptz,
                                                  toDate timestamptz,
                                                  dataSourceIds uuid[],
                                                  contributorIds bigint[],
                                                  contributedTo uuid[],
                                                  projectIds uuid[],
                                                  projectSlugs text[],
                                                  categoryIds uuid[],
                                                  languageIds uuid[],
                                                  ecosystemIds uuid[],
                                                  countryCodes text[],
                                                  contributionStatuses indexer_exp.contribution_status[],
                                                  searchQuery text,
                                                  filteredKpis boolean,
                                                  includeApplicants boolean)
    RETURNS TABLE
            (
                contributor_id            bigint,
                contributor_login         text,
                contributor_country       text,
                contributor               jsonb,
                first_project_name        text,
                projects                  jsonb,
                categories                jsonb,
                languages                 jsonb,
                ecosystems                jsonb,
                total_rewarded_usd_amount numeric,
                reward_count              bigint,
                issue_count               bigint,
                pr_count                  bigint,
                code_review_count         bigint,
                contribution_count        bigint
            )
    STABLE
    PARALLEL SAFE
AS
$$
SELECT c.contributor_id                  as contributor_id,
       c.contributor_login               as contributor_login,
       c.contributor_country             as contributor_country,
       c.contributor                     as contributor,
       c.first_project_name              as first_project_name,
       c.projects                        as projects,
       c.categories                      as categories,
       c.languages                       as languages,
       c.ecosystems                      as ecosystems,
       sum(rd.total_rewarded_usd_amount) as total_rewarded_usd_amount,
       sum(rd.reward_count)              as reward_count,
       sum(cd.issue_count)               as issue_count,
       sum(cd.pr_count)                  as pr_count,
       sum(cd.code_review_count)         as code_review_count,
       sum(cd.contribution_count)        as contribution_count
FROM bi.p_contributor_global_data c
         JOIN bi.p_contributor_reward_data crd ON crd.contributor_id = c.contributor_id
         JOIN bi.p_contributor_application_data cad ON cad.contributor_id = c.contributor_id

         LEFT JOIN (select cd.contributor_id,
                           count(cd.contribution_uuid)         as contribution_count,
                           coalesce(sum(cd.is_issue), 0)       as issue_count,
                           coalesce(sum(cd.is_pr), 0)          as pr_count,
                           coalesce(sum(cd.is_code_review), 0) as code_review_count
                    from bi.p_per_contributor_contribution_data cd
                    where (fromDate is null or cd.timestamp >= fromDate)
                      and (toDate is null or cd.timestamp < toDate)
                      and (dataSourceIds is null or cd.project_id = any (dataSourceIds) or
                           cd.program_ids && dataSourceIds or cd.ecosystem_ids && dataSourceIds)
                      and (contributionStatuses is null or cd.contribution_status = any (contributionStatuses))
                      and (not filteredKpis or projectIds is null or cd.project_id = any (projectIds))
                      and (not filteredKpis or projectSlugs is null or cd.project_slug = any (projectSlugs))
                      and (not filteredKpis or ecosystemIds is null or cd.ecosystem_ids && ecosystemIds)
                      and (not filteredKpis or categoryIds is null or cd.project_category_ids && categoryIds)
                      and (not filteredKpis or languageIds is null or cd.language_ids && languageIds)
                    group by cd.contributor_id) cd on cd.contributor_id = c.contributor_id

         LEFT JOIN (select rd.contributor_id,
                           count(rd.reward_id)             as reward_count,
                           coalesce(sum(rd.usd_amount), 0) as total_rewarded_usd_amount
                    from bi.p_reward_data rd
                    where (fromDate is null or rd.timestamp >= fromDate)
                      and (toDate is null or rd.timestamp < toDate)
                      and (dataSourceIds is null or rd.project_id = any (dataSourceIds) or
                           rd.program_ids && dataSourceIds or rd.ecosystem_ids && dataSourceIds)
                      and (not filteredKpis or projectIds is null or rd.project_id = any (projectIds))
                      and (not filteredKpis or projectSlugs is null or rd.project_slug = any (projectSlugs))
                      and (not filteredKpis or ecosystemIds is null or rd.ecosystem_ids && ecosystemIds)
                      and (not filteredKpis or categoryIds is null or rd.project_category_ids && categoryIds)
                      and (not filteredKpis or languageIds is null or rd.language_ids && languageIds)
                    group by rd.contributor_id) rd on rd.contributor_id = c.contributor_id

         LEFT JOIN (select ad.contributor_id,
                           count(ad.application_id)                                         as application_count,
                           count(ad.application_id) filter ( where ad.status = 'PENDING' )  as pending_application_count,
                           count(ad.application_id) filter ( where ad.status = 'ACCEPTED' ) as accepted_application_count,
                           count(ad.application_id) filter ( where ad.status = 'SHELVED' )  as shelved_application_count
                    from bi.p_application_data ad
                    where (fromDate is null or ad.timestamp >= fromDate)
                      and (toDate is null or ad.timestamp < toDate)
                      and (dataSourceIds is null or ad.project_id = any (dataSourceIds) or
                           ad.program_ids && dataSourceIds or ad.ecosystem_ids && dataSourceIds)
                      and (not filteredKpis or projectIds is null or ad.project_id = any (projectIds))
                      and (not filteredKpis or projectSlugs is null or ad.project_slug = any (projectSlugs))
                      and (not filteredKpis or ecosystemIds is null or ad.ecosystem_ids && ecosystemIds)
                      and (not filteredKpis or categoryIds is null or ad.project_category_ids && categoryIds)
                      and (not filteredKpis or languageIds is null or ad.language_ids && languageIds)
                    group by ad.contributor_id) ad on includeApplicants and ad.contributor_id = c.contributor_id

WHERE (dataSourceIds is null or
       c.contributed_on_project_ids && dataSourceIds or
       cad.applied_on_project_ids && dataSourceIds or
       c.program_ids && dataSourceIds or
       c.ecosystem_ids && dataSourceIds)
  and (contributorIds is null or c.contributor_id = any (contributorIds))
  and (projectIds is null or c.contributed_on_project_ids && projectIds or (includeApplicants and cad.applied_on_project_ids && projectIds))
  and (projectSlugs is null or c.contributed_on_project_slugs && projectSlugs or (includeApplicants and cad.applied_on_project_slugs && projectSlugs))
  and (ecosystemIds is null or c.ecosystem_ids && ecosystemIds)
  and (categoryIds is null or c.project_category_ids && categoryIds)
  and (languageIds is null or c.language_ids && languageIds)
  and (countryCodes is null or c.contributor_country = any (countryCodes))
  and (searchQuery is null or c.search ilike '%' || searchQuery || '%' or crd.search ilike '%' || searchQuery || '%')
  and (contributedTo is null or exists(select 1
                                       from bi.p_per_contributor_contribution_data cd
                                       where cd.contributor_id = c.contributor_id
                                         and cd.contribution_uuid = any (contributedTo)))
  and (cd.contributor_id is not null or rd.contributor_id is not null or ad.contributor_id is not null)
GROUP BY c.contributor_id, c.contributor_login, c.contributor, c.first_project_name,
         c.projects, c.ecosystems,
         c.languages, c.categories, c.contributor_country;
$$
    LANGUAGE SQL;

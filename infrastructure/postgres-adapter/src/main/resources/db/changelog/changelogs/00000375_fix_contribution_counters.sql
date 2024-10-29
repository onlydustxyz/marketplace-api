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
                maintained_projects       jsonb,
                total_rewarded_usd_amount numeric,
                reward_count              bigint,
                issue_count               bigint,
                pr_count                  bigint,
                code_review_count         bigint,
                contribution_count        bigint,
                in_progress_issue_count   bigint,
                pending_application_count bigint
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
       c.maintained_projects             as maintained_projects,
       sum(rd.total_rewarded_usd_amount) as total_rewarded_usd_amount,
       sum(rd.reward_count)              as reward_count,
       sum(cd.issue_count)               as issue_count,
       sum(cd.pr_count)                  as pr_count,
       sum(cd.code_review_count)         as code_review_count,
       sum(cd.contribution_count)        as contribution_count,
       sum(cd.in_progress_issue_count)   as in_progress_issue_count,
       sum(ad.pending_application_count) as pending_application_count
FROM bi.p_contributor_global_data c
         JOIN bi.p_contributor_reward_data crd ON crd.contributor_id = c.contributor_id
         JOIN bi.p_contributor_application_data cad ON cad.contributor_id = c.contributor_id

         LEFT JOIN (select cd.contributor_id,
                           count(cd.contribution_uuid)                                                           as contribution_count,
                           coalesce(sum(cd.is_issue), 0)                                                         as issue_count,
                           coalesce(sum(cd.is_pr), 0)                                                            as pr_count,
                           coalesce(sum(cd.is_code_review), 0)                                                   as code_review_count,
                           coalesce(sum(cd.is_issue) filter ( where cd.contribution_status = 'IN_PROGRESS' ), 0) as in_progress_issue_count
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

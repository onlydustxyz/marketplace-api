CREATE FUNCTION bi.select_projects(fromDate timestamptz,
                                   toDate timestamptz,
                                   programOrEcosystemIds uuid[],
                                   projectLeadIds uuid[],
                                   categoryIds uuid[],
                                   languageIds uuid[],
                                   ecosystemIds uuid[],
                                   searchQuery text)
    RETURNS TABLE
            (
                project_id                  uuid,
                project_name                text,
                project                     jsonb,
                leads                       jsonb,
                categories                  jsonb,
                languages                   jsonb,
                ecosystems                  jsonb,
                programs                    jsonb,
                budget                      jsonb,
                available_budget_usd        numeric,
                percent_spent_budget_usd    numeric,
                total_granted_usd_amount    numeric,
                contribution_count          bigint,
                reward_count                bigint,
                total_rewarded_usd_amount   numeric,
                average_reward_usd_amount   numeric,
                merged_pr_count             bigint,
                active_contributor_count    bigint,
                onboarded_contributor_count bigint
            )
AS
$$
SELECT p.project_id                        as project_id,
       p.project_name                      as project_name,
       p.project                           as project,
       p.leads                             as leads,
       p.categories                        as categories,
       p.languages                         as languages,
       p.ecosystems                        as ecosystems,
       p.programs                          as programs,
       p.budget                            as budget,
       p.available_budget_usd              as available_budget_usd,
       p.percent_spent_budget_usd          as percent_spent_budget_usd,
       sum(gd.total_granted_usd_amount)    as total_granted_usd_amount,
       sum(cd.contribution_count)          as contribution_count,
       sum(rd.reward_count)                as reward_count,
       sum(rd.total_rewarded_usd_amount)   as total_rewarded_usd_amount,
       sum(rd.average_reward_usd_amount)   as average_reward_usd_amount,
       sum(cd.merged_pr_count)             as merged_pr_count,
       sum(cd.active_contributor_count)    as active_contributor_count,
       sum(cd.onboarded_contributor_count) as onboarded_contributor_count
FROM bi.project_global_data p

         LEFT JOIN (select cd.project_id,
                           count(cd.contribution_id)                                                               as contribution_count,
                           coalesce(sum(cd.is_merged_pr), 0)                                                       as merged_pr_count,
                           count(distinct cd.contributor_id)                                                       as active_contributor_count,
                           count(distinct cd.contributor_id) filter ( where cd.is_first_contribution_on_onlydust ) as onboarded_contributor_count
                    from bi.contribution_data cd
                    where cd.timestamp >= fromDate
                      and cd.timestamp < toDate
                      and (languageIds is null or cd.language_ids && languageIds)
                    group by cd.project_id) cd on cd.project_id = p.project_id

         LEFT JOIN (select rd.project_id,
                           count(rd.reward_id)             as reward_count,
                           coalesce(sum(rd.usd_amount), 0) as total_rewarded_usd_amount,
                           coalesce(avg(rd.usd_amount), 0) as average_reward_usd_amount
                    from bi.reward_data rd
                    where rd.timestamp >= fromDate
                      and rd.timestamp < toDate
                    group by rd.project_id) rd on rd.project_id = p.project_id

         LEFT JOIN (select gd.project_id,
                           coalesce(sum(gd.usd_amount), 0) as total_granted_usd_amount
                    from bi.project_grants_data gd
                    where gd.timestamp >= fromDate
                      and gd.timestamp < toDate
                    group by gd.project_id) gd on gd.project_id = p.project_id

WHERE (p.program_ids && programOrEcosystemIds or p.ecosystem_ids && programOrEcosystemIds)
  and (ecosystemIds is null or p.ecosystem_ids && ecosystemIds)
  and (projectLeadIds is null or p.project_lead_ids && projectLeadIds)
  and (categoryIds is null or p.project_category_ids && categoryIds)
  and (languageIds is null or p.language_ids && languageIds)
  and (searchQuery is null or p.search ilike '%' || searchQuery || '%')
  and (cd.project_id is not null or rd.project_id is not null or gd.project_id is not null)

GROUP BY p.project_id, p.project_name, p.project, p.programs, p.ecosystems, p.languages, p.categories,
         p.leads, p.budget, p.available_budget_usd, p.percent_spent_budget_usd
$$
    LANGUAGE SQL;
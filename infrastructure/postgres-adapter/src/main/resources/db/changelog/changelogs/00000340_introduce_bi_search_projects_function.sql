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
                available_budget            numeric,
                percent_spent_budget        numeric,
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
SELECT unions.project_id                                                                         as project_id,
       sum(unions.total_granted_usd_amount) - sum(unions.total_rewarded_usd_amount)              as available_budget,
       sum(unions.total_granted_usd_amount) / greatest(sum(unions.total_rewarded_usd_amount), 1) as percent_spent_budget,
       sum(unions.total_granted_usd_amount)                                                      as total_granted_usd_amount,
       sum(unions.contribution_count)                                                            as contribution_count,
       sum(unions.reward_count)                                                                  as reward_count,
       sum(unions.total_rewarded_usd_amount)                                                     as total_rewarded_usd_amount,
       sum(unions.average_reward_usd_amount)                                                     as average_reward_usd_amount,
       sum(unions.merged_pr_count)                                                               as merged_pr_count,
       sum(unions.active_contributor_count)                                                      as active_contributor_count,
       sum(unions.onboarded_contributor_count)                                                   as onboarded_contributor_count
FROM (select cd.project_id,
             count(cd.contribution_id)                                                               as contribution_count,
             coalesce(sum(cd.is_merged_pr), 0)                                                       as merged_pr_count,
             count(distinct cd.contributor_id)                                                       as active_contributor_count,
             count(distinct cd.contributor_id) filter ( where cd.is_first_contribution_on_onlydust ) as onboarded_contributor_count,
             NULL::bigint                                                                            as reward_count,
             NULL::numeric                                                                           as total_rewarded_usd_amount,
             NULL::numeric                                                                           as average_reward_usd_amount,
             NULL::numeric                                                                           as total_granted_usd_amount
      from bi.contribution_data cd
      where cd.timestamp >= fromDate
        and cd.timestamp < toDate
        and (languageIds is null or cd.language_ids && languageIds)

      group by cd.project_id

      UNION

      select rd.project_id,
             NULL::bigint                    as contribution_count,
             NULL::numeric                   as merged_pr_count,
             NULL::bigint                    as active_contributor_count,
             NULL::bigint                    as onboarded_contributor_count,
             count(rd.reward_id)             as reward_count,
             coalesce(sum(rd.usd_amount), 0) as total_rewarded_usd_amount,
             coalesce(avg(rd.usd_amount), 0) as average_reward_usd_amount,
             NULL::numeric                   as total_granted_usd_amount
      from bi.reward_data rd
      where rd.timestamp >= fromDate
        and rd.timestamp < toDate
      group by rd.project_id

      UNION

      select gd.project_id,
             NULL::bigint                    as contribution_count,
             NULL::numeric                   as merged_pr_count,
             NULL::bigint                    as active_contributor_count,
             NULL::bigint                    as onboarded_contributor_count,
             NULL::bigint                    as reward_count,
             NULL::numeric                   as total_rewarded_usd_amount,
             NULL::numeric                   as average_reward_usd_amount,
             coalesce(sum(gd.usd_amount), 0) as total_granted_usd_amount
      from bi.project_grants_data gd
      where gd.timestamp >= fromDate
        and gd.timestamp < toDate
      group by gd.project_id) unions

         join bi.project_global_data p on unions.project_id = p.project_id and
                                          (p.program_ids && programOrEcosystemIds or p.ecosystem_ids && programOrEcosystemIds) and
                                          (ecosystemIds is null or p.ecosystem_ids && ecosystemIds) and
                                          (projectLeadIds is null or p.project_lead_ids && projectLeadIds) and
                                          (categoryIds is null or p.project_category_ids && categoryIds) and
                                          (languageIds is null or p.language_ids && languageIds) and
                                          (searchQuery is null or p.search ilike '%' || searchQuery || '%')

group by unions.project_id, p.project_id
$$
    LANGUAGE SQL;
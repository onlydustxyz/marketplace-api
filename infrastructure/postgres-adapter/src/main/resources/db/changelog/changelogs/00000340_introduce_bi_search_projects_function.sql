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
                project_id                         uuid,
                available_budget                   numeric,
                percent_spent_budget               numeric,
                total_granted_usd_amount           numeric,
                total_granted_amount_per_currency  jsonb,
                contribution_count                 bigint,
                reward_count                       bigint,
                total_rewarded_usd_amount          numeric,
                average_reward_usd_amount          numeric,
                total_rewarded_amount_per_currency jsonb,
                merged_pr_count                    bigint,
                active_contributor_count           bigint,
                onboarded_contributor_count        bigint
            )
AS
$$
SELECT unions.project_id                                                                         as project_id,
       sum(unions.total_granted_usd_amount) - sum(unions.total_rewarded_usd_amount)              as available_budget,
       sum(unions.total_granted_usd_amount) / greatest(sum(unions.total_rewarded_usd_amount), 1) as percent_spent_budget,
       sum(unions.total_granted_usd_amount)                                                      as total_granted_usd_amount,
       jsonb_agg(unions.total_granted_amount_per_currency)
       filter ( where unions.total_granted_amount_per_currency is not null )                     as total_granted_amount_per_currency,
       sum(unions.contribution_count)                                                            as contribution_count,
       sum(unions.reward_count)                                                                  as reward_count,
       sum(unions.total_rewarded_usd_amount)                                                     as total_rewarded_usd_amount,
       sum(unions.average_reward_usd_amount)                                                     as average_reward_usd_amount,
       jsonb_agg(unions.total_rewarded_amount_per_currency)
       filter ( where unions.total_rewarded_amount_per_currency is not null )                    as total_rewarded_amount_per_currency,
       sum(unions.merged_pr_count)                                                               as merged_pr_count,
       sum(unions.active_contributor_count)                                                      as active_contributor_count,
       sum(unions.onboarded_contributor_count)                                                   as onboarded_contributor_count
FROM (SELECT d.project_id                                                                          as project_id,
             NULL::numeric                                                                         as total_granted_usd_amount,
             NULL::jsonb                                                                           as total_granted_amount_per_currency,
             count(d.contribution_id)                                                              as contribution_count,
             NULL::bigint                                                                          as reward_count,
             NULL::numeric                                                                         as total_rewarded_usd_amount,
             NULL::numeric                                                                         as average_reward_usd_amount,
             NULL::jsonb                                                                           as total_rewarded_amount_per_currency,
             count(d.contribution_id) filter ( where d.is_merged_pr is true )                      as merged_pr_count,
             count(distinct d.contributor_id)                                                      as active_contributor_count,
             count(distinct d.contributor_id) filter ( where d.is_first_contribution_on_onlydust ) as onboarded_contributor_count
      from bi.contribution_data_cross_projects d
      where (fromDate is null or d.timestamp >= fromDate)
        and (toDate is null or d.timestamp < toDate)
        and (programOrEcosystemIds is null or d.program_ids && programOrEcosystemIds or
             d.ecosystem_ids && programOrEcosystemIds)
        and (projectLeadIds is null or d.project_lead_ids && projectLeadIds)
        and (categoryIds is null or d.project_category_ids && categoryIds)
        and (languageIds is null or d.language_ids && languageIds)
        and (ecosystemIds is null or d.ecosystem_ids && ecosystemIds)
        and (searchQuery is null or d.search ilike '%' || searchQuery || '%')
      group by d.project_id

      UNION

      SELECT d.project_id                                                                                as project_id,
             NULL::numeric                                                                               as total_granted_usd_amount,
             NULL::jsonb                                                                                 as total_granted_amount_per_currency,
             NULL::bigint                                                                                as contribution_count,
             sum(d.reward_count)                                                                         as reward_count,
             sum(d.total_usd_amount)                                                                     as total_rewarded_usd_amount,
             sum(d.total_usd_amount) / greatest(sum(d.reward_count), 1)                                  as average_reward_usd_amount,
             jsonb_agg(jsonb_build_object('currency_id', d.currency_id, 'total_amount', d.total_amount)) as total_rewarded_amount_per_currency,
             NULL::bigint                                                                                as merged_pr_count,
             NULL::bigint                                                                                as active_contributor_count,
             NULL::bigint                                                                                as onboarded_contributor_count
      FROM (SELECT d.project_id       as project_id,
                   count(d.reward_id) as reward_count,
                   sum(d.usd_amount)  as total_usd_amount,
                   sum(d.amount)      as total_amount,
                   d.currency_id      as currency_id
            from bi.reward_data d
            where (fromDate is null or d.timestamp >= fromDate)
              and (toDate is null or d.timestamp < toDate)
              and (programOrEcosystemIds is null or d.program_ids && programOrEcosystemIds or
                   d.ecosystem_ids && programOrEcosystemIds)
              and (projectLeadIds is null or d.project_lead_ids && projectLeadIds)
              and (categoryIds is null or d.project_category_ids && categoryIds)
              and (languageIds is null or d.language_ids && languageIds)
              and (ecosystemIds is null or d.ecosystem_ids && ecosystemIds)
              and (searchQuery is null or d.search ilike '%' || searchQuery || '%')
            group by d.project_id, d.currency_id) d
      group by d.project_id

      UNION

      SELECT d.project_id                                                                                                as project_id,
             sum(d.total_granted_usd_amount)                                                                             as total_granted_usd_amount,
             jsonb_agg(jsonb_build_object('currency_id', d.currency_id, 'total_granted_amount', d.total_granted_amount)) as total_granted_amount_per_currency,
             NULL::bigint                                                                                                as contribution_count,
             NULL::bigint                                                                                                as reward_count,
             NULL::numeric                                                                                               as total_rewarded_usd_amount,
             NULL::numeric                                                                                               as average_reward_usd_amount,
             NULL::jsonb                                                                                                 as total_rewarded_amount_per_currency,
             NULL::bigint                                                                                                as merged_pr_count,
             NULL::bigint                                                                                                as active_contributor_count,
             NULL::bigint                                                                                                as onboarded_contributor_count
      FROM (SELECT d.project_id      as project_id,
                   sum(d.usd_amount) as total_granted_usd_amount,
                   sum(d.amount)     as total_granted_amount,
                   d.currency_id     as currency_id
            from bi.project_grants_data d
            where (fromDate is null or d.timestamp >= fromDate)
              and (toDate is null or d.timestamp < toDate)
              and (programOrEcosystemIds is null or d.program_ids && programOrEcosystemIds or
                   d.ecosystem_ids && programOrEcosystemIds)
              and (projectLeadIds is null or d.project_lead_ids && projectLeadIds)
              and (categoryIds is null or d.project_category_ids && categoryIds)
              and (ecosystemIds is null or d.ecosystem_ids && ecosystemIds)
              and (searchQuery is null or d.search ilike '%' || searchQuery || '%')
            group by d.project_id, d.currency_id) d
      group by d.project_id) unions
group by unions.project_id
$$
    LANGUAGE SQL;


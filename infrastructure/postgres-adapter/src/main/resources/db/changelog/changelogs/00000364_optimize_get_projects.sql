call create_pseudo_projection('bi', 'contribution_data', $$
with project_contributions AS (select distinct on (c.id) c.*,
                                                         pgr.project_id as project_id,
                                                         p.slug         as project_slug
                               from indexer_exp.contributions c
                                        join project_github_repos pgr on pgr.github_repo_id = c.repo_id
                                        join projects p on p.id = pgr.project_id),
     registered_users as (select u.id             as id,
                                 u.github_user_id as github_user_id,
                                 kyc.country      as country
                          from iam.users u
                                   join accounting.billing_profiles_users bpu on bpu.user_id = u.id
                                   join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id)
select c.id                                                                                             as contribution_id,
       c.repo_id                                                                                        as repo_id,
       c.project_id                                                                                     as project_id,
       c.project_slug                                                                                   as project_slug,
       c.contributor_id                                                                                 as contributor_id,
       ru.id                                                                                            as contributor_user_id,
       ru.country                                                                                       as contributor_country,
       c.created_at                                                                                     as timestamp,
       c.status                                                                                         as contribution_status,
       date_trunc('day', c.created_at)                                                                  as day_timestamp,
       date_trunc('week', c.created_at)                                                                 as week_timestamp,
       date_trunc('month', c.created_at)                                                                as month_timestamp,
       date_trunc('quarter', c.created_at)                                                              as quarter_timestamp,
       date_trunc('year', c.created_at)                                                                 as year_timestamp,
       c.created_at = first.created_at                                                                  as is_first_contribution_on_onlydust,
       (c.type = 'ISSUE')::int                                                                          as is_issue,
       (c.type = 'PULL_REQUEST')::int                                                                   as is_pr,
       (c.type = 'CODE_REVIEW')::int                                                                    as is_code_review,
       array_agg(distinct lfe.language_id) filter ( where lfe.language_id is not null )                 as language_ids,
       array_agg(distinct pe.ecosystem_id) filter ( where pe.ecosystem_id is not null )                 as ecosystem_ids,
       array_agg(distinct pp.program_id) filter ( where pp.program_id is not null )                     as program_ids,
       array_agg(distinct ppc.project_category_id) filter ( where ppc.project_category_id is not null ) as project_category_ids,
       string_agg(distinct lfe.name, ' ')                                                               as languages
from project_contributions c
         join (select cc.contributor_id, min(cc.created_at) as created_at
               from project_contributions cc
               group by cc.contributor_id) first
              on first.contributor_id = c.contributor_id
         left join lateral ( select distinct lfe_1.language_id, l.name
                             from language_file_extensions lfe_1
                                      join languages l on l.id = lfe_1.language_id
                             where lfe_1.extension = any (c.main_file_extensions)) lfe on true
         left join projects_ecosystems pe on pe.project_id = c.project_id
         left join v_programs_projects pp on pp.project_id = c.project_id
         left join projects_project_categories ppc on ppc.project_id = c.project_id
         left join registered_users ru on ru.github_user_id = c.contributor_id
group by c.id,
         c.repo_id,
         c.project_id,
         c.project_slug,
         c.contributor_id,
         c.created_at,
         c.type,
         c.status,
         first.created_at,
         ru.id,
         ru.country
$$, 'contribution_id');



call create_pseudo_projection('bi', 'project_global_data', $$
SELECT p.id                                                                                                       as project_id,
       p.slug                                                                                                     as project_slug,
       p.created_at                                                                                               as created_at,
       jsonb_build_object('id', p.id,
                          'slug', p.slug,
                          'name', p.name,
                          'logoUrl', p.logo_url)                                                                  as project,
       p.name                                                                                                     as project_name,
       max(budgets.available_budget_usd)                                                                          as available_budget_usd,
       max(budgets.percent_spent_budget_usd)                                                                      as percent_spent_budget_usd,
       coalesce(jsonb_agg(distinct jsonb_build_object('availableBudgetUsd', budgets.available_budget_usd,
                                                      'percentSpentBudgetUsd', budgets.percent_spent_budget_usd,
                                                      'availableBudgetPerCurrency', budgets.available_budget_per_currency,
                                                      'percentSpentBudgetPerCurrency', budgets.percent_spent_budget_per_currency,
                                                      'grantedAmountUsd', budgets.granted_amount_usd,
                                                      'grantedAmountPerCurrency', budgets.granted_amount_per_currency,
                                                      'rewardedAmountUsd', budgets.rewarded_amount_usd,
                                                      'rewardedAmountPerCurrency', budgets.rewarded_amount_per_currency))
                filter ( where budgets.project_id is not null ), '[]'::jsonb) -> 0                                as budget,
       array_agg(distinct u.id) filter ( where u.id is not null )                                                 as project_lead_ids,
       array_agg(distinct pc.id) filter ( where pc.id is not null )                                               as project_category_ids,
       array_agg(distinct l.id) filter ( where l.id is not null )                                                 as language_ids,
       array_agg(distinct e.id) filter ( where e.id is not null )                                                 as ecosystem_ids,
       array_agg(distinct prog.id) filter ( where prog.id is not null )                                           as program_ids,

       jsonb_agg(distinct jsonb_build_object('id', u.id,
                                             'login', u.github_login,
                                             'githubUserId', u.github_user_id,
                                             'avatarUrl', u.github_avatar_url)) filter ( where u.id is not null ) as leads,

       jsonb_agg(distinct jsonb_build_object('id', pc.id,
                                             'slug', pc.slug,
                                             'name', pc.name,
                                             'description', pc.description,
                                             'iconSlug', pc.icon_slug)) filter ( where pc.id is not null )        as categories,

       jsonb_agg(distinct jsonb_build_object('id', l.id,
                                             'slug', l.slug,
                                             'name', l.name,
                                             'logoUrl', l.logo_url,
                                             'bannerUrl', l.banner_url)) filter ( where l.id is not null )        as languages,

       jsonb_agg(distinct jsonb_build_object('id', e.id,
                                             'slug', e.slug,
                                             'name', e.name,
                                             'logoUrl', e.logo_url,
                                             'bannerUrl', e.banner_url,
                                             'url', e.url)) filter ( where e.id is not null )                     as ecosystems,

       jsonb_agg(distinct jsonb_build_object('id', prog.id,
                                             'name', prog.name,
                                             'logoUrl', prog.logo_url)) filter ( where prog.id is not null )      as programs,
       concat(coalesce(string_agg(distinct u.github_login, ' '), ''), ' ',
              coalesce(string_agg(distinct p.name, ' '), ''), ' ',
              coalesce(string_agg(distinct p.slug, ' '), ''), ' ',
              coalesce(string_agg(distinct pc.name, ' '), ''), ' ',
              coalesce(string_agg(distinct l.name, ' '), ''), ' ',
              coalesce(string_agg(distinct e.name, ' '), ''), ' ',
              coalesce(string_agg(distinct currencies.name, ' '), ''), ' ',
              coalesce(string_agg(distinct currencies.code, ' '), ''), ' ',
              coalesce(string_agg(distinct prog.name, ' '), ''))                                                  as search
FROM projects p
         LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
         LEFT JOIN ecosystems e ON e.id = pe.ecosystem_id
         LEFT JOIN project_languages pl ON pl.project_id = p.id
         LEFT JOIN languages l ON l.id = pl.language_id
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
         LEFT JOIN project_categories pc ON pc.id = ppc.project_category_id
         LEFT JOIN v_programs_projects pp ON pp.project_id = p.id
         LEFT JOIN programs prog ON prog.id = pp.program_id
         LEFT JOIN project_leads pleads ON pleads.project_id = p.id
         LEFT JOIN iam.users u ON u.id = pleads.user_id
         LEFT JOIN LATERAL (select distinct c.name, c.code
                            from bi.p_reward_data rd
                                     full outer join bi.p_project_grants_data gd on gd.project_id = rd.project_id
                                     join currencies c on c.id = coalesce(rd.currency_id, gd.currency_id)
                            where rd.project_id = p.id
                               or gd.project_id = p.id) currencies on true
         LEFT JOIN LATERAL (select gd.project_id                                                                as project_id,
                                   coalesce(sum(gd.current_usd_amount), 0)                                      as granted_amount_usd,   -- sum of all granted amount, using their USD equivalent at the time of the grant
                                   coalesce(sum(rd.usd_amount), 0)                                              as rewarded_amount_usd,  -- sum of all rewarded amount, using their USD equivalent at the time of the reward
                                   sum(coalesce(gd.current_usd_amount, 0) - coalesce(rd.current_usd_amount, 0)) as available_budget_usd, -- available budget in USD, using the current USD equivalent of each currency

                                   sum(coalesce(rd.current_usd_amount, 0)) /
                                   greatest(sum(coalesce(gd.current_usd_amount, 0)), 1)                         as percent_spent_budget_usd,

                                   jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                'amount', gd.amount,
                                                                'usdAmount',
                                                                gd.current_usd_amount))                         as granted_amount_per_currency,

                                   jsonb_agg(jsonb_build_object('currency', rd.currency,
                                                                'amount', rd.amount,
                                                                'usdAmount', rd.usd_amount))
                                   filter ( where rd.currency_id is not null )                                  as rewarded_amount_per_currency,

                                   jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                'amount', coalesce(gd.amount, 0) - coalesce(rd.amount, 0),
                                                                'usdAmount',
                                                                coalesce(gd.current_usd_amount, 0) -
                                                                coalesce(rd.current_usd_amount, 0)))            as available_budget_per_currency,

                                   jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                'amount', coalesce(gd.amount, 0) /
                                                                          coalesce(rd.amount, 1)))              as percent_spent_budget_per_currency

                            from (select gd.project_id                                             as project_id,
                                         c.id                                                      as currency_id,
                                         jsonb_build_object('id', c.id,
                                                            'code', c.code,
                                                            'name', c.name,
                                                            'decimals', c.decimals,
                                                            'logoUrl', c.logo_url)                 as currency,
                                         sum(gd.amount)                                            as amount,
                                         accounting.usd_equivalent_at(sum(gd.amount), c.id, now()) as current_usd_amount
                                  from bi.p_project_grants_data gd
                                           join currencies c on c.id = gd.currency_id
                                  group by gd.project_id, c.id) gd

                                     left join (select rd.project_id                                             as project_id,
                                                       c.id                                                      as currency_id,
                                                       jsonb_build_object('id', c.id,
                                                                          'code', c.code,
                                                                          'name', c.name,
                                                                          'decimals', c.decimals,
                                                                          'logoUrl', c.logo_url)                 as currency,
                                                       sum(rd.usd_amount)                                        as usd_amount,
                                                       sum(rd.amount)                                            as amount,
                                                       accounting.usd_equivalent_at(sum(rd.amount), c.id, now()) as current_usd_amount
                                                from bi.p_reward_data rd
                                                         join currencies c on c.id = rd.currency_id
                                                group by rd.project_id, c.id) rd on gd.project_id = rd.project_id and gd.currency_id = rd.currency_id
                            group by gd.project_id ) budgets on budgets.project_id = p.id
GROUP BY p.id
$$, 'project_id');

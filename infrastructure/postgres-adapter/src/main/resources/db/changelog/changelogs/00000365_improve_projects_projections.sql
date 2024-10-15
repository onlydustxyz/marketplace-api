CREATE PROCEDURE refresh_pseudo_projection_unsafe(schema text, name text, pk_name text, condition text)
    LANGUAGE plpgsql
AS
$$
DECLARE
    view_name             text;
    projection_table_name text;
BEGIN
    view_name := 'v_' || name;
    projection_table_name := 'p_' || name;

    -- NOTE: to avoid deadlocks, rows are properly locked before being deleted.
    -- If a row is already locked, IT WILL BE SKIPPED, because we do not want this job to wait for the global-refresh to be done. We need it to be fast.
    EXECUTE format('delete from %I.%I where %I in ( select %I from %I.%I where %s for update skip locked )',
                   schema, projection_table_name, pk_name,
                   pk_name, schema, projection_table_name, condition);

    EXECUTE format('insert into %I.%I select * from %I.%I where %s on conflict (%I) do nothing',
                   schema, projection_table_name, schema, view_name, condition, pk_name);
END
$$;



CREATE OR REPLACE PROCEDURE refresh_pseudo_projection(schema text, name text, pk_name text, params jsonb)
    LANGUAGE plpgsql
AS
$$
DECLARE
    condition text;
    key       text;
    value     text;
BEGIN
    condition := ' true ';
    FOR key, value IN SELECT * FROM jsonb_each_text(params)
        LOOP
            condition := condition || format(' and %I = %L', key, value);
        END LOOP;

    CALL refresh_pseudo_projection_unsafe(schema, name, pk_name, condition);
END
$$;



call drop_pseudo_projection('bi', 'project_global_data');



call create_pseudo_projection('bi', 'project_global_data', $$
SELECT p.id                                                                                                  as project_id,
       p.slug                                                                                                as project_slug,
       p.created_at                                                                                          as created_at,
       p.rank                                                                                                as rank,
       jsonb_build_object('id', p.id,
                          'slug', p.slug,
                          'name', p.name,
                          'logoUrl', p.logo_url,
                          'shortDescription', p.short_description,
                          'hiring', p.hiring,
                          'visibility', p.visibility)                                                        as project,
       p.name                                                                                                as project_name,
       p.visibility                                                                                          as project_visibility,
       array_agg(distinct uleads.id) filter ( where uleads.id is not null )                                  as project_lead_ids,
       array_agg(distinct uinvleads.id) filter ( where uinvleads.id is not null )                            as invited_project_lead_ids,
       array_agg(distinct pc.id) filter ( where pc.id is not null )                                          as project_category_ids,
       array_agg(distinct pc.slug) filter ( where pc.slug is not null )                                      as project_category_slugs,
       array_agg(distinct l.id) filter ( where l.id is not null )                                            as language_ids,
       array_agg(distinct l.slug) filter ( where l.slug is not null )                                        as language_slugs,
       array_agg(distinct e.id) filter ( where e.id is not null )                                            as ecosystem_ids,
       array_agg(distinct e.slug) filter ( where e.slug is not null )                                        as ecosystem_slugs,
       array_agg(distinct prog.id) filter ( where prog.id is not null )                                      as program_ids,
       array_agg(distinct pgr.github_repo_id) filter ( where pgr.github_repo_id is not null )                as repo_ids,
       array_agg(distinct pt.tag) filter ( where pt.tag is not null )                                        as tags,

       jsonb_agg(distinct jsonb_build_object('id', uleads.id,
                                             'login', uleads.github_login,
                                             'githubUserId', uleads.github_user_id,
                                             'avatarUrl', user_avatar_url(uleads.github_user_id, uleads.github_avatar_url)
                          )) filter ( where uleads.id is not null )                                          as leads,

       jsonb_agg(distinct jsonb_build_object('id', pc.id,
                                             'slug', pc.slug,
                                             'name', pc.name,
                                             'description', pc.description,
                                             'iconSlug', pc.icon_slug)) filter ( where pc.id is not null )   as categories,

       jsonb_agg(distinct jsonb_build_object('id', l.id,
                                             'slug', l.slug,
                                             'name', l.name,
                                             'logoUrl', l.logo_url,
                                             'bannerUrl', l.banner_url)) filter ( where l.id is not null )   as languages,

       jsonb_agg(distinct jsonb_build_object('id', e.id,
                                             'slug', e.slug,
                                             'name', e.name,
                                             'logoUrl', e.logo_url,
                                             'bannerUrl', e.banner_url,
                                             'url', e.url)) filter ( where e.id is not null )                as ecosystems,

       jsonb_agg(distinct jsonb_build_object('id', prog.id,
                                             'name', prog.name,
                                             'logoUrl', prog.logo_url)) filter ( where prog.id is not null ) as programs,

       count(distinct pgr.github_repo_id) > count(distinct agr.repo_id)                                      as has_repos_without_github_app_installed,

       concat(coalesce(string_agg(distinct uleads.github_login, ' '), ''), ' ',
              coalesce(string_agg(distinct p.name, ' '), ''), ' ',
              coalesce(string_agg(distinct p.slug, ' '), ''), ' ',
              coalesce(string_agg(distinct pc.name, ' '), ''), ' ',
              coalesce(string_agg(distinct l.name, ' '), ''), ' ',
              coalesce(string_agg(distinct e.name, ' '), ''), ' ',
              coalesce(string_agg(distinct currencies.name, ' '), ''), ' ',
              coalesce(string_agg(distinct currencies.code, ' '), ''), ' ',
              coalesce(string_agg(distinct prog.name, ' '), ''))                                             as search
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
         LEFT JOIN iam.users uleads ON uleads.id = pleads.user_id
         LEFT JOIN pending_project_leader_invitations ppli ON ppli.project_id = p.id
         LEFT JOIN iam.users uinvleads ON uinvleads.github_user_id = ppli.github_user_id
         LEFT JOIN projects_tags pt ON pt.project_id = p.id
         LEFT JOIN project_github_repos pgr ON pgr.project_id = p.id
         LEFT JOIN indexer_exp.authorized_github_repos agr on agr.repo_id = pgr.github_repo_id
         LEFT JOIN LATERAL (select distinct c.name, c.code
                            from bi.p_reward_data rd
                                     full outer join bi.p_project_grants_data gd on gd.project_id = rd.project_id
                                     join currencies c on c.id = coalesce(rd.currency_id, gd.currency_id)
                            where rd.project_id = p.id
                               or gd.project_id = p.id) currencies on true
GROUP BY p.id
$$, 'project_id');



call create_pseudo_projection('bi', 'project_budget_data', $$
SELECT p.id                                                                                                  as project_id,
       max(budgets.available_budget_usd)                                                                     as available_budget_usd,
       max(budgets.percent_spent_budget_usd)                                                                 as percent_spent_budget_usd,
       coalesce(jsonb_agg(distinct jsonb_build_object('availableBudgetUsd', budgets.available_budget_usd,
                                                      'percentSpentBudgetUsd', budgets.percent_spent_budget_usd,
                                                      'availableBudgetPerCurrency', budgets.available_budget_per_currency,
                                                      'percentSpentBudgetPerCurrency', budgets.percent_spent_budget_per_currency,
                                                      'grantedAmountUsd', budgets.granted_amount_usd,
                                                      'grantedAmountPerCurrency', budgets.granted_amount_per_currency,
                                                      'rewardedAmountUsd', budgets.rewarded_amount_usd,
                                                      'rewardedAmountPerCurrency', budgets.rewarded_amount_per_currency))
                filter ( where budgets.project_id is not null ), '[]'::jsonb) -> 0                           as budget

FROM projects p
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



call create_pseudo_projection('bi', 'project_contributions_data', $$
SELECT p.id                                                                            as project_id,
       array_agg(distinct cd.repo_id)                                                  as repo_ids,
       count(distinct cd.contributor_id)                                               as contributor_count,
       count(cd.contribution_id) filter ( where cd.is_good_first_issue and
                                                coalesce(array_length(cd.assignee_ids, 1), 0) = 0 and
                                                cd.contribution_status != 'COMPLETED' and
                                                cd.contribution_status != 'CANCELLED') as good_first_issue_count
FROM projects p
         LEFT JOIN bi.p_contribution_data cd ON cd.project_id = p.id
GROUP BY p.id
$$, 'project_id');



create unique index on bi.p_project_global_data (project_slug);
create unique index on bi.p_project_budget_data (project_id, available_budget_usd, percent_spent_budget_usd);
create unique index on bi.p_project_contributions_data (project_id, contributor_count, good_first_issue_count);
create index on bi.p_project_contributions_data using gin (repo_ids);

create index bi_contribution_data_repo_id_idx on bi.p_contribution_data (repo_id);

create index bi_contribution_data_project_id_timestamp_idx on bi.p_contribution_data (project_id, timestamp);
create index bi_contribution_data_project_id_day_timestamp_idx on bi.p_contribution_data (project_id, day_timestamp);
create index bi_contribution_data_project_id_week_timestamp_idx on bi.p_contribution_data (project_id, week_timestamp);
create index bi_contribution_data_project_id_month_timestamp_idx on bi.p_contribution_data (project_id, month_timestamp);
create index bi_contribution_data_project_id_quarter_timestamp_idx on bi.p_contribution_data (project_id, quarter_timestamp);
create index bi_contribution_data_project_id_year_timestamp_idx on bi.p_contribution_data (project_id, year_timestamp);
create index bi_contribution_data_project_id_timestamp_idx_inv on bi.p_contribution_data (timestamp, project_id);
create index bi_contribution_data_project_id_day_timestamp_idx_inv on bi.p_contribution_data (day_timestamp, project_id);
create index bi_contribution_data_project_id_week_timestamp_idx_inv on bi.p_contribution_data (week_timestamp, project_id);
create index bi_contribution_data_project_id_month_timestamp_idx_inv on bi.p_contribution_data (month_timestamp, project_id);
create index bi_contribution_data_project_id_quarter_timestamp_idx_inv on bi.p_contribution_data (quarter_timestamp, project_id);

create index bi_contribution_data_project_id_year_timestamp_idx_inv on bi.p_contribution_data (year_timestamp, project_id);
create index bi_contribution_data_contributor_id_timestamp_idx on bi.p_contribution_data (contributor_id, timestamp);
create index bi_contribution_data_contributor_id_day_timestamp_idx on bi.p_contribution_data (contributor_id, day_timestamp);
create index bi_contribution_data_contributor_id_week_timestamp_idx on bi.p_contribution_data (contributor_id, week_timestamp);
create index bi_contribution_data_contributor_id_month_timestamp_idx on bi.p_contribution_data (contributor_id, month_timestamp);
create index bi_contribution_data_contributor_id_quarter_timestamp_idx on bi.p_contribution_data (contributor_id, quarter_timestamp);
create index bi_contribution_data_contributor_id_year_timestamp_idx on bi.p_contribution_data (contributor_id, year_timestamp);
create index bi_contribution_data_contributor_id_timestamp_idx_inv on bi.p_contribution_data (timestamp, contributor_id);
create index bi_contribution_data_contributor_id_day_timestamp_idx_inv on bi.p_contribution_data (day_timestamp, contributor_id);
create index bi_contribution_data_contributor_id_week_timestamp_idx_inv on bi.p_contribution_data (week_timestamp, contributor_id);
create index bi_contribution_data_contributor_id_month_timestamp_idx_inv on bi.p_contribution_data (month_timestamp, contributor_id);
create index bi_contribution_data_contributor_id_quarter_timestamp_idx_inv on bi.p_contribution_data (quarter_timestamp, contributor_id);

create index bi_contribution_data_contributor_id_year_timestamp_idx_inv on bi.p_contribution_data (year_timestamp, contributor_id);



CREATE OR REPLACE FUNCTION bi.select_projects(fromDate timestamptz,
                                              toDate timestamptz,
                                              dataSourceIds uuid[],
                                              programIds uuid[],
                                              projectIds uuid[],
                                              projectSlugs text[],
                                              projectLeadIds uuid[],
                                              categoryIds uuid[],
                                              languageIds uuid[],
                                              ecosystemIds uuid[],
                                              searchQuery text,
                                              showFilteredKpis boolean)
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
                reward_count                bigint,
                issue_count                 bigint,
                pr_count                    bigint,
                code_review_count           bigint,
                contribution_count          bigint,
                total_rewarded_usd_amount   numeric,
                average_reward_usd_amount   numeric,
                active_contributor_count    bigint,
                onboarded_contributor_count bigint
            )
    STABLE
    PARALLEL RESTRICTED
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
       pb.budget                           as budget,
       pb.available_budget_usd             as available_budget_usd,
       pb.percent_spent_budget_usd         as percent_spent_budget_usd,
       sum(gd.total_granted_usd_amount)    as total_granted_usd_amount,
       sum(rd.reward_count)                as reward_count,
       sum(cd.issue_count)                 as issue_count,
       sum(cd.pr_count)                    as pr_count,
       sum(cd.code_review_count)           as code_review_count,
       sum(cd.contribution_count)          as contribution_count,
       sum(rd.total_rewarded_usd_amount)   as total_rewarded_usd_amount,
       sum(rd.average_reward_usd_amount)   as average_reward_usd_amount,
       sum(cd.active_contributor_count)    as active_contributor_count,
       sum(cd.onboarded_contributor_count) as onboarded_contributor_count
FROM bi.p_project_global_data p
         JOIN bi.p_project_budget_data pb on pb.project_id = p.project_id

         LEFT JOIN (select cd.project_id,
                           count(cd.contribution_id)                                                               as contribution_count,
                           coalesce(sum(cd.is_issue), 0)                                                           as issue_count,
                           coalesce(sum(cd.is_pr), 0)                                                              as pr_count,
                           coalesce(sum(cd.is_code_review), 0)                                                     as code_review_count,
                           count(distinct cd.contributor_id)                                                       as active_contributor_count,
                           count(distinct cd.contributor_id) filter ( where cd.is_first_contribution_on_onlydust ) as onboarded_contributor_count
                    from bi.p_contribution_data cd
                    where cd.timestamp >= fromDate
                      and cd.timestamp < toDate
                      and (not showFilteredKpis or languageIds is null or cd.language_ids && languageIds)
                    group by cd.project_id) cd
                   on cd.project_id = p.project_id

         LEFT JOIN (select rd.project_id,
                           count(rd.reward_id)             as reward_count,
                           coalesce(sum(rd.usd_amount), 0) as total_rewarded_usd_amount,
                           coalesce(avg(rd.usd_amount), 0) as average_reward_usd_amount
                    from bi.p_reward_data rd
                    where rd.timestamp >= fromDate
                      and rd.timestamp < toDate
                      and (not showFilteredKpis or projectLeadIds is null or rd.requestor_id = any (projectLeadIds))
                      and (not showFilteredKpis or languageIds is null or rd.language_ids && languageIds)
                    group by rd.project_id) rd on rd.project_id = p.project_id

         LEFT JOIN (select gd.project_id,
                           coalesce(sum(gd.usd_amount), 0) as total_granted_usd_amount
                    from bi.p_project_grants_data gd
                    where gd.timestamp >= fromDate
                      and gd.timestamp < toDate
                    group by gd.project_id) gd on gd.project_id = p.project_id

WHERE (p.project_id = any (dataSourceIds) or p.program_ids && dataSourceIds or p.ecosystem_ids && dataSourceIds)
  and (ecosystemIds is null or p.ecosystem_ids && ecosystemIds)
  and (programIds is null or p.program_ids && programIds)
  and (projectIds is null or p.project_id = any (projectIds))
  and (projectSlugs is null or p.project_slug = any (projectSlugs))
  and (projectLeadIds is null or p.project_lead_ids && projectLeadIds)
  and (categoryIds is null or p.project_category_ids && categoryIds)
  and (languageIds is null or p.language_ids && languageIds)
  and (searchQuery is null or p.search ilike '%' || searchQuery || '%')
  and (cd.project_id is not null or rd.project_id is not null or gd.project_id is not null)

GROUP BY p.project_id, p.project_name, p.project, p.programs, p.ecosystems, p.languages, p.categories,
         p.leads, pb.budget, pb.available_budget_usd, pb.percent_spent_budget_usd
$$
    LANGUAGE SQL;
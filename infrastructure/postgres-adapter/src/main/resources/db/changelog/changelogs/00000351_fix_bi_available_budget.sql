DROP FUNCTION bi.select_projects(fromDate timestamptz,
                                 toDate timestamptz,
                                 programOrEcosystemIds uuid[],
                                 projectLeadIds uuid[],
                                 categoryIds uuid[],
                                 languageIds uuid[],
                                 ecosystemIds uuid[],
                                 searchQuery text);

DROP MATERIALIZED VIEW IF EXISTS bi.project_global_data;
DROP MATERIALIZED VIEW IF EXISTS bi.project_grants_data;



CREATE FUNCTION accounting.usd_equivalent_at(amount numeric, currency_id UUID, at timestamp with time zone)
    RETURNS NUMERIC AS
$$
SELECT amount * accounting.usd_quote_at(currency_id, at);
$$ LANGUAGE SQL;



CREATE MATERIALIZED VIEW bi.project_grants_data AS
SELECT abt.project_id,
       abt.program_id,
       abt.currency_id,
       abt.timestamp,
       date_trunc('day', abt.timestamp)                                                 as day_timestamp,
       date_trunc('week', abt.timestamp)                                                as week_timestamp,
       date_trunc('month', abt.timestamp)                                               as month_timestamp,
       date_trunc('quarter', abt.timestamp)                                             as quarter_timestamp,
       date_trunc('year', abt.timestamp)                                                as year_timestamp,
       CASE
           WHEN abt.type = 'TRANSFER' THEN abt.amount * hq.usd_conversion_rate
           ELSE abt.amount * hq.usd_conversion_rate * -1 END                            as usd_amount,
       CASE
           WHEN abt.type = 'TRANSFER' THEN abt.amount
           ELSE abt.amount * -1 END                                                     as amount,
       array_agg(distinct pe.ecosystem_id) filter ( where pe.ecosystem_id is not null ) as ecosystem_ids,
       coalesce(prog.name, '')                                                          as search
FROM accounting.account_book_transactions abt
         JOIN LATERAL (select accounting.usd_quote_at(abt.currency_id, abt.timestamp) as usd_conversion_rate) hq
              ON true
         JOIN programs prog ON prog.id = abt.program_id
         LEFT JOIN projects_ecosystems pe ON pe.project_id = abt.project_id
WHERE abt.project_id IS NOT NULL
  AND (abt.type = 'TRANSFER' OR abt.type = 'REFUND')
  AND abt.reward_id IS NULL
  AND abt.payment_id IS NULL
GROUP BY abt.project_id,
         abt.program_id,
         abt.currency_id,
         abt.timestamp,
         abt.type,
         abt.amount,
         hq.usd_conversion_rate,
         prog.name
;

create unique index bi_project_grants_data_pk on bi.project_grants_data (project_id, timestamp, program_id, currency_id);

create index bi_project_grants_data_timestamp_idx on bi.project_grants_data (timestamp, project_id);
create index bi_project_grants_data_day_timestamp_idx on bi.project_grants_data (day_timestamp, project_id);
create index bi_project_grants_data_week_timestamp_idx on bi.project_grants_data (week_timestamp, project_id);
create index bi_project_grants_data_month_timestamp_idx on bi.project_grants_data (month_timestamp, project_id);
create index bi_project_grants_data_quarter_timestamp_idx on bi.project_grants_data (quarter_timestamp, project_id);
create index bi_project_grants_data_year_timestamp_idx on bi.project_grants_data (year_timestamp, project_id);
create index bi_project_grants_data_timestamp_idx_inv on bi.project_grants_data (project_id, timestamp);
create index bi_project_grants_data_day_timestamp_idx_inv on bi.project_grants_data (project_id, day_timestamp);
create index bi_project_grants_data_week_timestamp_idx_inv on bi.project_grants_data (project_id, week_timestamp);
create index bi_project_grants_data_month_timestamp_idx_inv on bi.project_grants_data (project_id, month_timestamp);
create index bi_project_grants_data_quarter_timestamp_idx_inv on bi.project_grants_data (project_id, quarter_timestamp);
create index bi_project_grants_data_year_timestamp_idx_inv on bi.project_grants_data (project_id, year_timestamp);



CREATE MATERIALIZED VIEW bi.project_global_data AS
WITH project_programs AS (select distinct abt.program_id,
                                          abt.project_id
                          from accounting.account_book_transactions abt
                          where abt.project_id is not null
                            and abt.reward_id is null)
SELECT p.id                                                                                                       as project_id,
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
         LEFT JOIN project_programs pp ON pp.project_id = p.id
         LEFT JOIN programs prog ON prog.id = pp.program_id
         LEFT JOIN project_leads pleads ON pleads.project_id = p.id
         LEFT JOIN iam.users u ON u.id = pleads.user_id
         LEFT JOIN LATERAL (select distinct c.name, c.code
                            from bi.reward_data rd
                                     full outer join bi.project_grants_data gd on gd.project_id = rd.project_id
                                     join currencies c on c.id = coalesce(rd.currency_id, gd.currency_id)
                            where rd.project_id = p.id
                               or gd.project_id = p.id) currencies on true
         LEFT JOIN LATERAL ( select gd.project_id                                                      as project_id,
                                    coalesce(sum(gd.usd_amount), 0)                                    as granted_amount_usd,   -- sum of all granted amount, using their USD equivalent at the time of the grant
                                    coalesce(sum(rd.usd_amount), 0)                                    as rewarded_amount_usd,  -- sum of all rewarded amount, using their USD equivalent at the time of the reward
                                    sum(coalesce(accounting.usd_equivalent_at(coalesce(gd.amount, 0) - coalesce(rd.amount, 0), gd.currency_id, now()),
                                                 0))                                                   as available_budget_usd, -- available budget in USD, using the current USD equivalent of each currency

                                    sum(coalesce(accounting.usd_equivalent_at(rd.usd_amount, gd.currency_id, now()), 0)) /
                                    greatest(sum(coalesce(accounting.usd_equivalent_at(gd.usd_amount, gd.currency_id, now()), 0)),
                                             1)                                                        as percent_spent_budget_usd,

                                    jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                 'amount', gd.amount,
                                                                 'usdAmount',
                                                                 gd.usd_amount))                       as granted_amount_per_currency,

                                    jsonb_agg(jsonb_build_object('currency', rd.currency,
                                                                 'amount', rd.amount,
                                                                 'usdAmount', rd.usd_amount))
                                    filter ( where rd.currency_id is not null )                        as rewarded_amount_per_currency,

                                    jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                 'amount', coalesce(gd.amount, 0) - coalesce(rd.amount, 0),
                                                                 'usdAmount',
                                                                 accounting.usd_equivalent_at(coalesce(gd.amount, 0) - coalesce(rd.amount, 0), gd.currency_id,
                                                                                              now()))) as available_budget_per_currency,

                                    jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                 'amount', coalesce(gd.amount, 0) /
                                                                           coalesce(rd.amount, 1)))    as percent_spent_budget_per_currency

                             from (select gd.project_id                             as project_id,
                                          c.id                                      as currency_id,
                                          jsonb_build_object('id', c.id,
                                                             'code', c.code,
                                                             'name', c.name,
                                                             'decimals', c.decimals,
                                                             'logoUrl', c.logo_url) as currency,
                                          sum(gd.usd_amount)                        as usd_amount,
                                          sum(gd.amount)                            as amount
                                   from bi.project_grants_data gd
                                            join currencies c on c.id = gd.currency_id
                                   group by gd.project_id, c.id) gd

                                      left join (select rd.project_id                             as project_id,
                                                        c.id                                      as currency_id,
                                                        jsonb_build_object('id', c.id,
                                                                           'code', c.code,
                                                                           'name', c.name,
                                                                           'decimals', c.decimals,
                                                                           'logoUrl', c.logo_url) as currency,
                                                        sum(rd.usd_amount)                        as usd_amount,
                                                        sum(rd.amount)                            as amount
                                                 from bi.reward_data rd
                                                          join currencies c on c.id = rd.currency_id
                                                 group by rd.project_id, c.id) rd on gd.project_id = rd.project_id and gd.currency_id = rd.currency_id
                             group by gd.project_id ) budgets on budgets.project_id = p.id
GROUP BY p.id;


CREATE UNIQUE INDEX bi_project_global_data_pk ON bi.project_global_data (project_id);



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
       sum(rd.reward_count)                as reward_count,
       sum(cd.issue_count)                 as issue_count,
       sum(cd.pr_count)                    as pr_count,
       sum(cd.code_review_count)           as code_review_count,
       sum(cd.contribution_count)          as contribution_count,
       sum(rd.total_rewarded_usd_amount)   as total_rewarded_usd_amount,
       sum(rd.average_reward_usd_amount)   as average_reward_usd_amount,
       sum(cd.active_contributor_count)    as active_contributor_count,
       sum(cd.onboarded_contributor_count) as onboarded_contributor_count
FROM bi.project_global_data p

         LEFT JOIN (select cd.project_id,
                           count(cd.contribution_id)                                                               as contribution_count,
                           coalesce(sum(cd.is_issue), 0)                                                           as issue_count,
                           coalesce(sum(cd.is_pr), 0)                                                              as pr_count,
                           coalesce(sum(cd.is_code_review), 0)                                                     as code_review_count,
                           count(distinct cd.contributor_id)                                                       as active_contributor_count,
                           count(distinct cd.contributor_id) filter ( where cd.is_first_contribution_on_onlydust ) as onboarded_contributor_count
                    from bi.contribution_data cd
                    where cd.timestamp >= fromDate
                      and cd.timestamp
                        < toDate
                      and (languageIds is null
                        or cd.language_ids && languageIds)
                    group by cd.project_id) cd
                   on cd.project_id = p.project_id
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

WHERE (p.program_ids && programOrEcosystemIds
    or p.ecosystem_ids && programOrEcosystemIds)
  and (ecosystemIds is null
    or p.ecosystem_ids && ecosystemIds)
  and (projectLeadIds is null
    or p.project_lead_ids && projectLeadIds)
  and (categoryIds is null
    or p.project_category_ids && categoryIds)
  and (languageIds is null
    or p.language_ids && languageIds)
  and (searchQuery is null
    or p.search ilike '%' || searchQuery || '%')
  and (cd.project_id is not null
    or rd.project_id is not null
    or gd.project_id is not null)

GROUP BY p.project_id, p.project_name, p.project, p.programs, p.ecosystems, p.languages, p.categories,
         p.leads, p.budget, p.available_budget_usd, p.percent_spent_budget_usd
$$
    LANGUAGE SQL;

DROP MATERIALIZED VIEW IF EXISTS bi.project_global_data;
DROP MATERIALIZED VIEW IF EXISTS bi.project_data_unions;
DROP MATERIALIZED VIEW IF EXISTS bi.contribution_data_cross_projects;
DROP MATERIALIZED VIEW IF EXISTS bi.contribution_data;
DROP MATERIALIZED VIEW IF EXISTS bi.reward_data;
DROP MATERIALIZED VIEW IF EXISTS bi.daily_project_grants;
DROP MATERIALIZED VIEW IF EXISTS bi_internal.contribution_contributor_timestamps;
DROP MATERIALIZED VIEW IF EXISTS bi_internal.contribution_project_timestamps;
DROP VIEW IF EXISTS bi_internal.exploded_rewards;
DROP VIEW IF EXISTS bi_internal.exploded_contributions;
DROP SCHEMA IF EXISTS bi_internal;



CREATE MATERIALIZED VIEW bi.contribution_data AS
WITH completed_contributions AS (select distinct on (c.id) c.*,
                                                           pgr.project_id as project_id
                                 from indexer_exp.contributions c
                                          join project_github_repos pgr on pgr.github_repo_id = c.repo_id
                                 where c.status = 'COMPLETED'),
     exploded_contributions AS
         (select c.id                            as contribution_id,
                 c.contributor_id                as contributor_id,
                 c.created_at                    as timestamp,
                 c.project_id                    as project_id,
                 lfe.language_id                 as language_id,
                 c.type = 'PULL_REQUEST'         as is_merged_pr,
                 c.created_at = first.created_at as is_first_contribution_on_onlydust
          from completed_contributions c
                   join (select cc.contributor_id, min(cc.created_at) as created_at
                         from completed_contributions cc
                         group by cc.contributor_id) first
                        on first.contributor_id = c.contributor_id
                   left join lateral ( select distinct lfe_1.language_id
                                       from language_file_extensions lfe_1
                                       where lfe_1.extension = any (c.main_file_extensions)) lfe on true)
SELECT c.*,
       coalesce(language_names.value, '') as search
FROM (with registered_users as (select u.id             as id,
                                       u.github_user_id as github_user_id,
                                       kyc.country      as country
                                from iam.users u
                                         join accounting.billing_profiles_users bpu on bpu.user_id = u.id
                                         join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id)
      select c.contribution_id                                                            as contribution_id,
             c.project_id                                                                 as project_id,
             c.contributor_id                                                             as contributor_id,
             ru.id                                                                        as contributor_user_id,
             ru.country                                                                   as contributor_country,
             c.timestamp                                                                  as timestamp,
             date_trunc('day', c.timestamp)                                               as day_timestamp,
             date_trunc('week', c.timestamp)                                              as week_timestamp,
             date_trunc('month', c.timestamp)                                             as month_timestamp,
             date_trunc('quarter', c.timestamp)                                           as quarter_timestamp,
             date_trunc('year', c.timestamp)                                              as year_timestamp,
             c.is_first_contribution_on_onlydust                                          as is_first_contribution_on_onlydust,
             c.is_merged_pr::int                                                          as is_merged_pr,
             array_agg(distinct c.language_id) filter ( where c.language_id is not null ) as language_ids
      from exploded_contributions c
               left join registered_users ru on ru.github_user_id = c.contributor_id
      group by c.contribution_id,
               c.project_id,
               c.contributor_id,
               c.timestamp,
               c.is_first_contribution_on_onlydust,
               c.is_merged_pr,
               ru.id,
               ru.country) c
         left join lateral (select string_agg(l.name, ' ') as value
                            from languages l
                            where l.id = any (c.language_ids)) as language_names on true
;

create unique index bi_contribution_data_pk on bi.contribution_data (contribution_id);
create index bi_contribution_data_contributor_id_idx on bi.contribution_data (contributor_id);
create index bi_contribution_data_timestamp_idx on bi.contribution_data (timestamp);
create index bi_contribution_data_day_timestamp_idx on bi.contribution_data (day_timestamp);
create index bi_contribution_data_week_timestamp_idx on bi.contribution_data (week_timestamp);
create index bi_contribution_data_month_timestamp_idx on bi.contribution_data (month_timestamp);
create index bi_contribution_data_quarter_timestamp_idx on bi.contribution_data (quarter_timestamp);
create index bi_contribution_data_year_timestamp_idx on bi.contribution_data (year_timestamp);
create index bi_contribution_data_contributor_id_timestamp_idx on bi.contribution_data (contributor_id, timestamp);
create index bi_contribution_data_contributor_id_day_timestamp_idx on bi.contribution_data (contributor_id, day_timestamp);
create index bi_contribution_data_contributor_id_week_timestamp_idx on bi.contribution_data (contributor_id, week_timestamp);
create index bi_contribution_data_contributor_id_month_timestamp_idx on bi.contribution_data (contributor_id, month_timestamp);
create index bi_contribution_data_contributor_id_quarter_timestamp_idx on bi.contribution_data (contributor_id, quarter_timestamp);
create index bi_contribution_data_contributor_id_year_timestamp_idx on bi.contribution_data (contributor_id, year_timestamp);
create index bi_contribution_data_project_id_timestamp_idx on bi.contribution_data (project_id, timestamp);
create index bi_contribution_data_timestamp_project_id_idx on bi.contribution_data (timestamp, project_id);



CREATE MATERIALIZED VIEW bi.reward_data AS
WITH exploded_rewards AS
         (SELECT r.id                      as reward_id,
                 r.requested_at            as timestamp,
                 r.recipient_id            as contributor_id,
                 r.project_id              as project_id,
                 r.currency_id             as currency_id,
                 r.amount                  as amount,
                 rsd.amount_usd_equivalent as usd_amount
          FROM rewards r
                   JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id)
SELECT r.*,
       coalesce(currency_search.value, '') as search
FROM (select r.reward_id                        as reward_id,
             r.timestamp                        as timestamp,
             date_trunc('day', r.timestamp)     as day_timestamp,
             date_trunc('week', r.timestamp)    as week_timestamp,
             date_trunc('month', r.timestamp)   as month_timestamp,
             date_trunc('quarter', r.timestamp) as quarter_timestamp,
             date_trunc('year', r.timestamp)    as year_timestamp,
             r.contributor_id                   as contributor_id,
             r.project_id                       as project_id,
             r.usd_amount                       as usd_amount,
             r.amount                           as amount,
             r.currency_id                      as currency_id
      from exploded_rewards r
      group by r.reward_id,
               r.timestamp,
               r.contributor_id,
               r.project_id,
               r.usd_amount,
               r.amount,
               r.currency_id) r
         left join lateral (select cur.name || ' ' || cur.code as value
                            from currencies cur
                            where cur.id = r.currency_id) as currency_search on true;

create unique index bi_reward_data_pk on bi.reward_data (reward_id);
create index bi_reward_data_project_id_timestamp_idx on bi.reward_data (project_id, timestamp);
create index bi_reward_data_timestamp_project_id_idx on bi.reward_data (timestamp, project_id);
create index bi_reward_data_day_timestamp_idx on bi.reward_data (day_timestamp, project_id, currency_id);
create index bi_reward_data_week_timestamp_idx on bi.reward_data (week_timestamp, project_id, currency_id);
create index bi_reward_data_month_timestamp_idx on bi.reward_data (month_timestamp, project_id, currency_id);
create index bi_reward_data_quarter_timestamp_idx on bi.reward_data (quarter_timestamp, project_id, currency_id);
create index bi_reward_data_year_timestamp_idx on bi.reward_data (year_timestamp, project_id, currency_id);


DROP MATERIALIZED VIEW IF EXISTS bi.project_grants_data;
CREATE MATERIALIZED VIEW bi.project_grants_data AS
SELECT abt.project_id,
       abt.program_id,
       abt.currency_id,
       abt.timestamp,
       date_trunc('day', abt.timestamp)                      as day_timestamp,
       date_trunc('week', abt.timestamp)                     as week_timestamp,
       date_trunc('month', abt.timestamp)                    as month_timestamp,
       date_trunc('quarter', abt.timestamp)                  as quarter_timestamp,
       date_trunc('year', abt.timestamp)                     as year_timestamp,
       CASE
           WHEN abt.type = 'TRANSFER' THEN abt.amount * hq.usd_conversion_rate
           ELSE abt.amount * hq.usd_conversion_rate * -1 END as usd_amount,
       CASE
           WHEN abt.type = 'TRANSFER' THEN abt.amount
           ELSE abt.amount * -1 END                          as amount,
       coalesce(prog.name, '')                               as search
FROM accounting.account_book_transactions abt
         JOIN LATERAL (select accounting.usd_quote_at(abt.currency_id, abt.timestamp) as usd_conversion_rate) hq
              ON true
         JOIN programs prog ON prog.id = abt.program_id
WHERE abt.project_id IS NOT NULL
  AND (abt.type = 'TRANSFER' OR abt.type = 'REFUND')
  AND abt.reward_id IS NULL
  AND abt.payment_id IS NULL
;
create unique index bi_project_grants_data_pk on bi.project_grants_data (project_id, timestamp, program_id, currency_id);
create index bi_project_grants_data_idx_inv on bi.project_grants_data (project_id, timestamp);
create index bi_project_grants_data_timestamp_idx on bi.project_grants_data (timestamp, project_id);
create index bi_project_grants_data_day_timestamp_idx on bi.project_grants_data (day_timestamp, project_id);
create index bi_project_grants_data_week_timestamp_idx on bi.project_grants_data (week_timestamp, project_id);
create index bi_project_grants_data_month_timestamp_idx on bi.project_grants_data (month_timestamp, project_id);
create index bi_project_grants_data_quarter_timestamp_idx on bi.project_grants_data (quarter_timestamp, project_id);
create index bi_project_grants_data_year_timestamp_idx on bi.project_grants_data (year_timestamp, project_id);



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
         LEFT JOIN LATERAL ( select gd.project_id                                                                  as project_id,
                                    coalesce(sum(gd.usd_amount), 0)                                                as granted_amount_usd,
                                    coalesce(sum(rd.usd_amount), 0)                                                as rewarded_amount_usd,
                                    sum(coalesce(gd.usd_amount, 0)) - sum(coalesce(rd.usd_amount, 0))              as available_budget_usd,
                                    sum(coalesce(rd.usd_amount, 0)) / greatest(sum(coalesce(gd.usd_amount, 0)), 1) as percent_spent_budget_usd,

                                    jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                 'amount', gd.amount,
                                                                 'usdAmount', gd.usd_amount))                      as granted_amount_per_currency,

                                    jsonb_agg(jsonb_build_object('currency', rd.currency,
                                                                 'amount', rd.amount,
                                                                 'usdAmount', rd.usd_amount))
                                    filter ( where rd.currency_id is not null )                                    as rewarded_amount_per_currency,

                                    jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                 'amount', coalesce(gd.amount, 0) - coalesce(rd.amount, 0),
                                                                 'usdAmount', coalesce(gd.usd_amount, 0) -
                                                                              coalesce(rd.usd_amount, 0)))         as available_budget_per_currency,

                                    jsonb_agg(jsonb_build_object('currency', gd.currency,
                                                                 'amount', coalesce(gd.amount, 0) / coalesce(rd.amount, 1),
                                                                 'usdAmount', coalesce(gd.usd_amount, 0) /
                                                                              coalesce(rd.usd_amount, 1)))         as percent_spent_budget_per_currency
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

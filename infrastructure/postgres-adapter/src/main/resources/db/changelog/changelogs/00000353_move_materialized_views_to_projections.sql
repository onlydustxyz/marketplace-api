CREATE OR REPLACE PROCEDURE create_pseudo_projection(schema text, name text, query text, pk_name text)
    LANGUAGE plpgsql
AS
$$
DECLARE
    view_name              text;
    materialized_view_name text;
    projection_table_name  text;
BEGIN
    view_name := 'v_' || name;
    materialized_view_name := 'm_' || name;
    projection_table_name := 'p_' || name;

    EXECUTE format('CREATE VIEW %I.%I AS SELECT v.*, md5(v::text) as hash from (%s) v', schema, view_name, query);

    EXECUTE format('CREATE MATERIALIZED VIEW %I.%I AS SELECT * FROM %I.%I', schema, materialized_view_name, schema, view_name);
    EXECUTE format('CREATE UNIQUE INDEX %I_%I_pk ON %I.%I (%I)', schema, 'm_' || name, schema, materialized_view_name, pk_name);
    EXECUTE format('CREATE UNIQUE INDEX %I_%I_%I_hash_idx ON %I.%I (%I, hash)', schema, 'm_' || name, pk_name, schema, materialized_view_name, pk_name);

    EXECUTE format('CREATE TABLE %I.%I AS TABLE %I.%I', schema, projection_table_name, schema, materialized_view_name);
    EXECUTE format('ALTER TABLE %I.%I ADD PRIMARY KEY (%I)', schema, projection_table_name, pk_name);
    EXECUTE format('CREATE UNIQUE INDEX %I_%I_%I_hash_idx ON %I.%I (%I, hash)', schema, 'p_' || name, pk_name, schema, projection_table_name, pk_name);

    EXECUTE format('ALTER TABLE %I.%I ADD COLUMN tech_created_at timestamptz NOT NULL DEFAULT now()', schema, projection_table_name);
    EXECUTE format('ALTER TABLE %I.%I ADD COLUMN tech_updated_at timestamptz NOT NULL DEFAULT now()', schema, projection_table_name);
    EXECUTE format('CREATE TRIGGER %s_%s_set_tech_updated_at ' ||
                   'BEFORE UPDATE ON %I.%I ' ||
                   'FOR EACH ROW EXECUTE FUNCTION set_tech_updated_at()', schema, name, schema, projection_table_name);
END
$$;

CREATE OR REPLACE PROCEDURE drop_pseudo_projection(schema text, name text)
    LANGUAGE plpgsql
AS
$$
DECLARE
    view_name              text;
    materialized_view_name text;
    projection_table_name  text;
BEGIN
    view_name := 'v_' || name;
    materialized_view_name := 'm_' || name;
    projection_table_name := 'p_' || name;

    EXECUTE format('DROP TABLE %I.%I', schema, projection_table_name);
    EXECUTE format('DROP MATERIALIZED VIEW %I.%I', schema, materialized_view_name);
    EXECUTE format('DROP VIEW %I.%I', schema, view_name);
END
$$;

CREATE OR REPLACE PROCEDURE refresh_pseudo_projection(schema text, name text, pk_name text)
    LANGUAGE plpgsql
AS
$$
DECLARE
    materialized_view_name text;
    projection_table_name  text;
BEGIN
    materialized_view_name := 'm_' || name;
    projection_table_name := 'p_' || name;

    EXECUTE format('refresh materialized view %I.%I', schema, materialized_view_name);

    EXECUTE format('delete from %I.%I where not exists(select 1 from %I.%I m where m.%I = %I.%I and m.hash = %I.hash)',
                   schema,
                   projection_table_name,
                   schema,
                   materialized_view_name,
                   pk_name,
                   projection_table_name,
                   pk_name,
                   projection_table_name);

    EXECUTE format('insert into %I.%I select * from %I.%I on conflict (%I) do nothing',
                   schema,
                   projection_table_name,
                   schema,
                   materialized_view_name,
                   pk_name);
END
$$;

CREATE OR REPLACE PROCEDURE refresh_pseudo_projection(schema text, name text, params jsonb)
    LANGUAGE plpgsql
AS
$$
DECLARE
    view_name             text;
    projection_table_name text;
    condition             text;
    key                   text;
    value                 text;
BEGIN
    view_name := 'v_' || name;
    projection_table_name := 'p_' || name;

    condition := '';
    FOR key, value IN SELECT * FROM jsonb_each_text(params)
        LOOP
            condition := condition || format(' and %I = %L', key, value);
        END LOOP;

    EXECUTE format('delete from %I.%I where true %s', schema, projection_table_name, condition);

    EXECUTE format('insert into %I.%I select * from %I.%I where true %s', schema, projection_table_name, schema, view_name, condition);
END
$$;

DROP FUNCTION bi.select_projects(fromDate timestamptz,
                                 toDate timestamptz,
                                 programOrEcosystemIds uuid[],
                                 projectIds uuid[],
                                 projectSlugs text[],
                                 projectLeadIds uuid[],
                                 categoryIds uuid[],
                                 languageIds uuid[],
                                 ecosystemIds uuid[],
                                 searchQuery text,
                                 showFilteredKpis boolean);

DROP FUNCTION bi.select_contributors(fromDate timestamptz,
                                     toDate timestamptz,
                                     programOrEcosystemIds uuid[],
                                     contributorIds bigint[],
                                     projectIds uuid[],
                                     projectSlugs text[],
                                     categoryIds uuid[],
                                     languageIds uuid[],
                                     ecosystemIds uuid[],
                                     countryCodes text[],
                                     contributionStatuses indexer_exp.contribution_status[],
                                     searchQuery text,
                                     filteredKpis boolean);

DROP MATERIALIZED VIEW IF EXISTS bi.m_project_global_data;
DROP MATERIALIZED VIEW IF EXISTS bi.m_contributor_global_data;
DROP MATERIALIZED VIEW IF EXISTS bi.m_contribution_data;
DROP MATERIALIZED VIEW IF EXISTS bi.m_reward_data;

DROP MATERIALIZED VIEW IF EXISTS bi.m_project_grants_data;
DROP VIEW active_programs_projects;

DROP MATERIALIZED VIEW m_programs_projects;
DROP VIEW accounting.all_transactions;

ALTER TABLE accounting.account_book_transactions
    ADD COLUMN deposit_status accounting.deposit_status;

ALTER TABLE accounting.account_book_transactions
    RENAME TO all_transactions;

CREATE VIEW v_programs_projects AS
WITH allocations AS (SELECT abt.currency_id,
                            abt.program_id,
                            abt.project_id,
                            COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'TRANSFER' AND reward_id IS NULL), 0)
                                - COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'REFUND' AND reward_id IS NULL), 0)
                                - COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'TRANSFER' AND reward_id IS NOT NULL), 0)
                                + COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'REFUND' AND reward_id IS NOT NULL), 0) AS remaining_amount
                     FROM accounting.all_transactions abt
                     WHERE abt.project_id IS NOT NULL
                       AND abt.payment_id IS NULL
                     GROUP BY abt.currency_id, abt.program_id, abt.project_id)
SELECT program_id, project_id, bool_or(remaining_amount > 0) as has_remaining_grants
FROM allocations
GROUP BY program_id, project_id;

CREATE MATERIALIZED VIEW m_active_programs_projects AS
SELECT program_id, project_id
FROM v_programs_projects
WHERE has_remaining_grants IS TRUE;

CREATE UNIQUE INDEX m_active_programs_projects_pk
    ON m_active_programs_projects (program_id, project_id);

CREATE UNIQUE INDEX m_active_programs_projects_pk_inv
    ON m_active_programs_projects (project_id, program_id);

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

call create_pseudo_projection('bi', 'reward_data', $$
select r.id                                                                                             as reward_id,
       r.requested_at                                                                                   as timestamp,
       date_trunc('day', r.requested_at)                                                                as day_timestamp,
       date_trunc('week', r.requested_at)                                                               as week_timestamp,
       date_trunc('month', r.requested_at)                                                              as month_timestamp,
       date_trunc('quarter', r.requested_at)                                                            as quarter_timestamp,
       date_trunc('year', r.requested_at)                                                               as year_timestamp,
       r.recipient_id                                                                                   as contributor_id,
       r.requestor_id                                                                                   as requestor_id,
       r.project_id                                                                                     as project_id,
       p.slug                                                                                           as project_slug,
       rsd.amount_usd_equivalent                                                                        as usd_amount,
       r.amount                                                                                         as amount,
       r.currency_id                                                                                    as currency_id,
       array_agg(distinct pe.ecosystem_id) filter ( where pe.ecosystem_id is not null )                 as ecosystem_ids,
       array_agg(distinct pp.program_id) filter ( where pp.program_id is not null )                     as program_ids,
       array_agg(distinct lfe.language_id) filter ( where lfe.language_id is not null )                 as language_ids,
       array_agg(distinct ppc.project_category_id) filter ( where ppc.project_category_id is not null ) as project_category_ids,
       string_agg(currencies.name || ' ' || currencies.code, ' ')                                       as search
from rewards r
         join accounting.reward_status_data rsd ON rsd.reward_id = r.id
         join projects p on p.id = r.project_id
         join currencies on currencies.id = r.currency_id
         left join projects_ecosystems pe on pe.project_id = r.project_id
         left join v_programs_projects pp on pp.project_id = r.project_id
         left join projects_project_categories ppc on ppc.project_id = r.project_id
         left join reward_items ri on r.id = ri.reward_id
         left join indexer_exp.contributions c on c.contributor_id = ri.recipient_id and
                                                  c.repo_id = ri.repo_id and
                                                  c.github_number = ri.number and
                                                  c.type::text::contribution_type = ri.type
         left join language_file_extensions lfe on lfe.extension = any (c.main_file_extensions)
group by r.id,
         r.requested_at,
         r.recipient_id,
         r.project_id,
         rsd.amount_usd_equivalent,
         r.amount,
         r.currency_id,
         p.slug
    $$, 'reward_id');

create index bi_p_reward_data_project_id_timestamp_idx on bi.p_reward_data (project_id, timestamp, currency_id);
create index bi_p_reward_data_project_id_day_timestamp_idx on bi.p_reward_data (project_id, day_timestamp, currency_id);
create index bi_p_reward_data_project_id_week_timestamp_idx on bi.p_reward_data (project_id, week_timestamp, currency_id);
create index bi_p_reward_data_project_id_month_timestamp_idx on bi.p_reward_data (project_id, month_timestamp, currency_id);
create index bi_p_reward_data_project_id_quarter_timestamp_idx on bi.p_reward_data (project_id, quarter_timestamp, currency_id);
create index bi_p_reward_data_project_id_year_timestamp_idx on bi.p_reward_data (project_id, year_timestamp, currency_id);
create index bi_p_reward_data_project_id_timestamp_idx_inv on bi.p_reward_data (timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_day_timestamp_idx_inv on bi.p_reward_data (day_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_week_timestamp_idx_inv on bi.p_reward_data (week_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_month_timestamp_idx_inv on bi.p_reward_data (month_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_quarter_timestamp_idx_inv on bi.p_reward_data (quarter_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_year_timestamp_idx_inv on bi.p_reward_data (year_timestamp, project_id, currency_id);

create index bi_p_reward_data_contributor_id_timestamp_idx on bi.p_reward_data (contributor_id, timestamp, currency_id);
create index bi_p_reward_data_contributor_id_day_timestamp_idx on bi.p_reward_data (contributor_id, day_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_week_timestamp_idx on bi.p_reward_data (contributor_id, week_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_month_timestamp_idx on bi.p_reward_data (contributor_id, month_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_quarter_timestamp_idx on bi.p_reward_data (contributor_id, quarter_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_year_timestamp_idx on bi.p_reward_data (contributor_id, year_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_timestamp_idx_inv on bi.p_reward_data (timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_day_timestamp_idx_inv on bi.p_reward_data (day_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_week_timestamp_idx_inv on bi.p_reward_data (week_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_month_timestamp_idx_inv on bi.p_reward_data (month_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_quarter_timestamp_idx_inv on bi.p_reward_data (quarter_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_year_timestamp_idx_inv on bi.p_reward_data (year_timestamp, contributor_id, currency_id);

call create_pseudo_projection('bi', 'project_grants_data', $$
SELECT abt.id                                                                           as transaction_id,
       abt.project_id                                                                   as project_id,
       abt.program_id                                                                   as program_id,
       abt.currency_id                                                                  as currency_id,
       abt.timestamp                                                                    as timestamp,
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
FROM accounting.all_transactions abt
         JOIN LATERAL (select accounting.usd_quote_at(abt.currency_id, abt.timestamp) as usd_conversion_rate) hq
              ON true
         JOIN programs prog ON prog.id = abt.program_id
         LEFT JOIN projects_ecosystems pe ON pe.project_id = abt.project_id
WHERE abt.project_id IS NOT NULL
  AND (abt.type = 'TRANSFER' OR abt.type = 'REFUND')
  AND abt.reward_id IS NULL
  AND abt.payment_id IS NULL
GROUP BY abt.id,
         abt.project_id,
         abt.program_id,
         abt.currency_id,
         abt.timestamp,
         abt.type,
         abt.amount,
         hq.usd_conversion_rate,
         prog.name
$$, 'transaction_id');

create unique index bi_project_grants_data_project_timestamp_program_currency_idx on bi.p_project_grants_data (project_id, timestamp, program_id, currency_id);
create index bi_project_grants_data_timestamp_idx on bi.p_project_grants_data (timestamp, project_id);
create index bi_project_grants_data_day_timestamp_idx on bi.p_project_grants_data (day_timestamp, project_id);
create index bi_project_grants_data_week_timestamp_idx on bi.p_project_grants_data (week_timestamp, project_id);
create index bi_project_grants_data_month_timestamp_idx on bi.p_project_grants_data (month_timestamp, project_id);
create index bi_project_grants_data_quarter_timestamp_idx on bi.p_project_grants_data (quarter_timestamp, project_id);
create index bi_project_grants_data_year_timestamp_idx on bi.p_project_grants_data (year_timestamp, project_id);
create index bi_project_grants_data_timestamp_idx_inv on bi.p_project_grants_data (project_id, timestamp);
create index bi_project_grants_data_day_timestamp_idx_inv on bi.p_project_grants_data (project_id, day_timestamp);
create index bi_project_grants_data_week_timestamp_idx_inv on bi.p_project_grants_data (project_id, week_timestamp);
create index bi_project_grants_data_month_timestamp_idx_inv on bi.p_project_grants_data (project_id, month_timestamp);
create index bi_project_grants_data_quarter_timestamp_idx_inv on bi.p_project_grants_data (project_id, quarter_timestamp);
create index bi_project_grants_data_year_timestamp_idx_inv on bi.p_project_grants_data (project_id, year_timestamp);



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


call create_pseudo_projection('bi', 'contributor_global_data', $$
WITH user_countries as (select bpu.user_id as user_id,
                              kyc.country as country
                       from accounting.billing_profiles_users bpu
                                join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id)
SELECT u.github_user_id                                                                                    as contributor_id,
       u.login                                                                                             as contributor_login,
       uc.country                                                                                          as contributor_country,
       jsonb_build_object('githubUserId', u.github_user_id,
                          'login', u.login,
                          'avatarUrl', u.avatar_url,
                          'isRegistered', u.user_id is not null,
                          'id',
                          u.user_id)                                                                       as contributor,

       min(p.name)                                                                                         as first_project_name,

       jsonb_agg(distinct jsonb_build_object('id', p.id,
                                             'slug', p.slug,
                                             'name', p.name,
                                             'logoUrl', p.logo_url)) filter ( where p.id is not null )     as projects,

       array_agg(distinct p.id) filter ( where p.id is not null )                                          as project_ids,
       array_agg(distinct p.slug) filter ( where p.slug is not null )                                      as project_slugs,
       array_agg(distinct pc.id) filter ( where pc.id is not null )                                        as project_category_ids,
       array_agg(distinct l.id) filter ( where l.id is not null )                                          as language_ids,
       array_agg(distinct e.id) filter ( where e.id is not null )                                          as ecosystem_ids,
       array_agg(distinct prog.id) filter ( where prog.id is not null )                                    as program_ids,

       jsonb_agg(distinct jsonb_build_object('id', pc.id,
                                             'slug', pc.slug,
                                             'name', pc.name,
                                             'description', pc.description,
                                             'iconSlug', pc.icon_slug))
       filter ( where pc.id is not null )                                                                  as categories,

       jsonb_agg(distinct jsonb_build_object('id', l.id,
                                             'slug', l.slug,
                                             'name', l.name,
                                             'logoUrl', l.logo_url,
                                             'bannerUrl', l.banner_url)) filter ( where l.id is not null ) as languages,

       jsonb_agg(distinct jsonb_build_object('id', e.id,
                                             'slug', e.slug,
                                             'name', e.name,
                                             'logoUrl', e.logo_url,
                                             'bannerUrl', e.banner_url,
                                             'url', e.url))
       filter ( where e.id is not null )                                                                   as ecosystems,

       concat(coalesce(string_agg(distinct u.login, ' '), ''), ' ',
              coalesce(string_agg(distinct p.name, ' '), ''), ' ',
              coalesce(string_agg(distinct p.slug, ' '), ''), ' ',
              coalesce(string_agg(distinct pc.name, ' '), ''), ' ',
              coalesce(string_agg(distinct l.name, ' '), ''), ' ',
              coalesce(string_agg(distinct e.name, ' '), ''), ' ',
              coalesce(string_agg(distinct currencies.name, ' '), ''), ' ',
              coalesce(string_agg(distinct currencies.code, ' '), ''), ' ',
              coalesce(string_agg(distinct prog.name, ' '), ''))                                           as search
FROM iam.all_users u
         LEFT JOIN user_countries uc on uc.user_id = u.user_id
         LEFT JOIN bi.v_contribution_data cd on cd.contributor_id = u.github_user_id
         LEFT JOIN projects p on p.id = cd.project_id
         LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
         LEFT JOIN ecosystems e ON e.id = pe.ecosystem_id
         LEFT JOIN v_programs_projects pp ON pp.project_id = p.id
         LEFT JOIN programs prog ON prog.id = pp.program_id
         LEFT JOIN languages l ON l.id = any(cd.language_ids)
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
         LEFT JOIN project_categories pc ON pc.id = ppc.project_category_id
         LEFT JOIN rewards r on r.recipient_id = u.github_user_id
         LEFT JOIN currencies on r.currency_id = currencies.id
GROUP BY u.github_user_id,
         u.login,
         u.avatar_url,
         u.user_id,
         uc.country
$$, 'contributor_id');


CREATE FUNCTION bi.select_projects(fromDate timestamptz,
                                   toDate timestamptz,
                                   programOrEcosystemIds uuid[],
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
FROM bi.p_project_global_data p

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

WHERE (p.program_ids && programOrEcosystemIds or p.ecosystem_ids && programOrEcosystemIds)
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
         p.leads, p.budget, p.available_budget_usd, p.percent_spent_budget_usd
$$
    LANGUAGE SQL;



CREATE FUNCTION bi.select_contributors(fromDate timestamptz,
                                       toDate timestamptz,
                                       programOrEcosystemIds uuid[],
                                       contributorIds bigint[],
                                       projectIds uuid[],
                                       projectSlugs text[],
                                       categoryIds uuid[],
                                       languageIds uuid[],
                                       ecosystemIds uuid[],
                                       countryCodes text[],
                                       contributionStatuses indexer_exp.contribution_status[],
                                       searchQuery text,
                                       filteredKpis boolean)
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

         LEFT JOIN (select cd.contributor_id,
                           count(cd.contribution_id)           as contribution_count,
                           coalesce(sum(cd.is_issue), 0)       as issue_count,
                           coalesce(sum(cd.is_pr), 0)          as pr_count,
                           coalesce(sum(cd.is_code_review), 0) as code_review_count
                    from bi.p_contribution_data cd
                    where cd.timestamp >= fromDate
                      and cd.timestamp < toDate
                      and (cd.program_ids && programOrEcosystemIds or cd.ecosystem_ids && programOrEcosystemIds)
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
                    where rd.timestamp >= fromDate
                      and rd.timestamp < toDate
                      and (rd.program_ids && programOrEcosystemIds or rd.ecosystem_ids && programOrEcosystemIds)
                      and (not filteredKpis or projectIds is null or rd.project_id = any (projectIds))
                      and (not filteredKpis or projectSlugs is null or rd.project_slug = any (projectSlugs))
                      and (not filteredKpis or ecosystemIds is null or rd.ecosystem_ids && ecosystemIds)
                      and (not filteredKpis or categoryIds is null or rd.project_category_ids && categoryIds)
                      and (not filteredKpis or languageIds is null or rd.language_ids && languageIds)
                    group by rd.contributor_id) rd on rd.contributor_id = c.contributor_id

WHERE (c.program_ids && programOrEcosystemIds or c.ecosystem_ids && programOrEcosystemIds)
  and (contributorIds is null or c.contributor_id = any (contributorIds))
  and (projectIds is null or c.project_ids && projectIds)
  and (projectSlugs is null or c.project_slugs && projectSlugs)
  and (ecosystemIds is null or c.ecosystem_ids && ecosystemIds)
  and (categoryIds is null or c.project_category_ids && categoryIds)
  and (languageIds is null or c.language_ids && languageIds)
  and (countryCodes is null or c.contributor_country = any (countryCodes))
  and (searchQuery is null or c.search ilike '%' || searchQuery || '%')
  and (cd.contributor_id is not null or rd.contributor_id is not null)

GROUP BY c.contributor_id, c.contributor_login, c.contributor, c.first_project_name, c.projects, c.ecosystems,
         c.languages, c.categories, c.contributor_country;
$$
    LANGUAGE SQL;
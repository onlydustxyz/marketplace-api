DROP MATERIALIZED VIEW bi.project_global_data;
DROP MATERIALIZED VIEW bi.project_data_unions;
DROP MATERIALIZED VIEW bi.contribution_data_cross_projects;
DROP MATERIALIZED VIEW bi.contribution_data;
DROP MATERIALIZED VIEW bi.reward_data;
DROP MATERIALIZED VIEW bi.daily_project_grants;


CREATE MATERIALIZED VIEW bi.contribution_data AS
SELECT c.*,
       language_names.value || ' ' ||
       ecosystem_names.value || ' ' ||
       program_names.value || ' ' ||
       project_names.value || ' ' ||
       project_category_names.value as search
FROM (with registered_users as (select u.id             as id,
                                       u.github_user_id as github_user_id,
                                       kyc.country      as country
                                from iam.users u
                                         join accounting.billing_profiles_users bpu on bpu.user_id = u.id
                                         join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id)
      select c.contribution_id                         as contribution_id,
             c.contributor_id                          as contributor_id,
             ru.id                                     as contributor_user_id,
             ru.country                                as contributor_country,
             c.timestamp                               as timestamp,
             date_trunc('day', c.timestamp)            as day_timestamp,
             date_trunc('week', c.timestamp)           as week_timestamp,
             date_trunc('month', c.timestamp)          as month_timestamp,
             date_trunc('quarter', c.timestamp)        as quarter_timestamp,
             date_trunc('year', c.timestamp)           as year_timestamp,
             c.is_first_contribution_on_onlydust       as is_first_contribution_on_onlydust,
             c.is_merged_pr                            as is_merged_pr,
             array_agg(distinct c.language_id)         as language_ids,
             array_agg(distinct c.ecosystem_id)        as ecosystem_ids,
             array_agg(distinct c.program_id)          as program_ids,
             array_agg(distinct c.project_id)          as project_ids,
             array_agg(distinct c.project_category_id) as project_category_ids
      from bi_internal.exploded_contributions c
               left join registered_users ru on ru.github_user_id = c.contributor_id
      group by c.contribution_id,
               c.contributor_id,
               c.timestamp,
               c.is_first_contribution_on_onlydust,
               c.is_merged_pr,
               ru.id,
               ru.country) c
         left join lateral (select string_agg(l.name, ' ') as value from languages l where l.id = any (c.language_ids)) as language_names on true
         left join lateral (select string_agg(e.name, ' ') as value from ecosystems e where e.id = any (c.ecosystem_ids)) as ecosystem_names on true
         left join lateral (select string_agg(p.name, ' ') as value from programs p where p.id = any (c.program_ids)) as program_names on true
         left join lateral (select string_agg(p.name, ' ') as value from projects p where p.id = any (c.program_ids)) as project_names on true
         left join lateral (select string_agg(p.name, ' ') as value
                            from project_categories p
                            where p.id = any (c.project_category_ids)) as project_category_names on true
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



CREATE MATERIALIZED VIEW bi.contribution_data_cross_projects AS
select c.*,
       projects.id as project_id
from bi.contribution_data c
         CROSS JOIN unnest(c.project_ids) AS projects(id)
where projects.id is not null;

create unique index bi_cdcp_pk on bi.contribution_data_cross_projects (contribution_id, project_id);
create index bi_cdcp_timestamp_program_ids_ecosystem_ids_idx on bi.contribution_data_cross_projects (timestamp, program_ids, ecosystem_ids);
create index bi_contribution_data_proj_project_id_idx on bi.contribution_data_cross_projects (project_id);
create index bi_contribution_data_proj_timestamp_idx on bi.contribution_data_cross_projects (timestamp);
create index bi_contribution_data_proj_day_timestamp_idx on bi.contribution_data_cross_projects (day_timestamp);
create index bi_contribution_data_proj_week_timestamp_idx on bi.contribution_data_cross_projects (week_timestamp);
create index bi_contribution_data_proj_month_timestamp_idx on bi.contribution_data_cross_projects (month_timestamp);
create index bi_contribution_data_proj_quarter_timestamp_idx on bi.contribution_data_cross_projects (quarter_timestamp);
create index bi_contribution_data_proj_year_timestamp_idx on bi.contribution_data_cross_projects (year_timestamp);
create index bi_contribution_data_proj_contributor_id_timestamp_idx on bi.contribution_data_cross_projects (project_id, timestamp);
create index bi_contribution_data_proj_contributor_id_day_timestamp_idx on bi.contribution_data_cross_projects (project_id, day_timestamp);
create index bi_contribution_data_proj_contributor_id_week_timestamp_idx on bi.contribution_data_cross_projects (project_id, week_timestamp);
create index bi_contribution_data_proj_contributor_id_month_timestamp_idx on bi.contribution_data_cross_projects (project_id, month_timestamp);
create index bi_contribution_data_proj_contributor_id_quarter_timestamp_idx on bi.contribution_data_cross_projects (project_id, quarter_timestamp);
create index bi_contribution_data_proj_contributor_id_year_timestamp_idx on bi.contribution_data_cross_projects (project_id, year_timestamp);



CREATE OR REPLACE VIEW bi_internal.exploded_rewards AS
SELECT r.id                      as reward_id,
       r.requested_at            as timestamp,
       r.recipient_id            as contributor_id,
       r.project_id              as project_id,
       r.currency_id             as currency_id,
       rsd.amount_usd_equivalent as usd_amount,
       ppc.project_category_id   as project_category_id,
       lfe.language_id           as language_id,
       pe.ecosystem_id           as ecosystem_id,
       pp.program_id             as program_id
FROM rewards r
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
         LEFT JOIN projects_ecosystems pe ON pe.project_id = r.project_id
         LEFT JOIN programs_projects pp ON pp.project_id = r.project_id
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = r.project_id

         LEFT JOIN reward_items ri ON ri.reward_id = r.id
         LEFT JOIN completed_contributions c
                   ON ri.type = c.type::text::contribution_type
                       AND ri.number = c.github_number
                       AND r.recipient_id = c.contributor_id
         LEFT JOIN LATERAL ( SELECT DISTINCT lfe_1.language_id
                             FROM language_file_extensions lfe_1
                             WHERE lfe_1.extension = ANY (c.main_file_extensions)) lfe ON true
;


CREATE MATERIALIZED VIEW bi.reward_data AS
SELECT c.*,
       language_names.value || ' ' ||
       ecosystem_names.value || ' ' ||
       program_names.value || ' ' ||
       project_names.value || ' ' ||
       project_category_names.value || ' ' ||
       currency_search.value as search
FROM (select r.reward_id                               as reward_id,
             r.timestamp                               as timestamp,
             date_trunc('day', r.timestamp)            as day_timestamp,
             date_trunc('week', r.timestamp)           as week_timestamp,
             date_trunc('month', r.timestamp)          as month_timestamp,
             date_trunc('quarter', r.timestamp)        as quarter_timestamp,
             date_trunc('year', r.timestamp)           as year_timestamp,
             r.contributor_id                          as contributor_id,
             r.project_id                              as project_id,
             r.usd_amount                              as usd_amount,
             r.currency_id                             as currency_id,
             array_agg(distinct r.language_id)         as language_ids,
             array_agg(distinct r.ecosystem_id)        as ecosystem_ids,
             array_agg(distinct r.program_id)          as program_ids,
             array_agg(distinct r.project_category_id) as project_category_ids
      from bi_internal.exploded_rewards r
      group by r.reward_id,
               r.timestamp,
               r.contributor_id,
               r.project_id,
               r.usd_amount) c
         left join lateral (select cur.name || ' ' || cur.code as value from currencies cur where cur.id = c.currency_id) as currency_search on true
         left join lateral (select string_agg(l.name, ' ') as value from languages l where l.id = any (c.language_ids)) as language_names on true
         left join lateral (select string_agg(e.name, ' ') as value from ecosystems e where e.id = any (c.ecosystem_ids)) as ecosystem_names on true
         left join lateral (select string_agg(p.name, ' ') as value from programs p where p.id = any (c.program_ids)) as program_names on true
         left join lateral (select string_agg(p.name, ' ') as value from projects p where p.id = any (c.program_ids)) as project_names on true
         left join lateral (select string_agg(p.name, ' ') as value
                            from project_categories p
                            where p.id = any (c.project_category_ids)) as project_category_names on true;

create unique index bi_reward_data_pk on bi.reward_data (reward_id);
create index bi_reward_data_timestamp_program_ids_ecosystem_ids_idx on bi.reward_data (timestamp, program_ids, ecosystem_ids);
create index bi_reward_data_day_timestamp_idx on bi.reward_data (day_timestamp, program_ids, ecosystem_ids);
create index bi_reward_data_week_timestamp_idx on bi.reward_data (week_timestamp, program_ids, ecosystem_ids);
create index bi_reward_data_month_timestamp_idx on bi.reward_data (month_timestamp, program_ids, ecosystem_ids);
create index bi_reward_data_quarter_timestamp_idx on bi.reward_data (quarter_timestamp, program_ids, ecosystem_ids);
create index bi_reward_data_year_timestamp_idx on bi.reward_data (year_timestamp, program_ids, ecosystem_ids);



CREATE MATERIALIZED VIEW bi.daily_project_grants(project_id, program_id, day_timestamp, usd_amount) AS
SELECT c.*,
       currency_search.value as search
FROM (select abt.project_id,
             abt.program_id,
             abt.currency_id,
             date_trunc('day', abt.timestamp) as day_timestamp,
             sum(abt.usd_amount)
      from (SELECT abt.project_id,
                   abt.program_id,
                   abt.currency_id,
                   CASE
                       WHEN abt.type = 'TRANSFER' THEN abt.amount * hq.usd_conversion_rate
                       ELSE abt.amount * hq.usd_conversion_rate * -1 END as usd_amount,
                   abt.timestamp
            FROM accounting.account_book_transactions abt
                     JOIN LATERAL (select accounting.usd_quote_at(abt.currency_id, abt.timestamp) as usd_conversion_rate) hq ON true
            WHERE abt.project_id IS NOT NULL
              AND (abt.type = 'TRANSFER' OR abt.type = 'REFUND')
              AND abt.reward_id IS NULL
              AND abt.payment_id IS NULL) abt
      group by 1, 2, 3, 4) c
         left join lateral (select cur.name || ' ' || cur.code as value from currencies cur where cur.id = c.currency_id) as currency_search on true
;
create unique index bi_daily_project_grants_pk on bi.daily_project_grants (project_id, program_id, day_timestamp);
create unique index bi_daily_project_grants_pk_inv on bi.daily_project_grants (day_timestamp, program_id, project_id);



CREATE MATERIALIZED VIEW bi.project_data_unions AS
SELECT *,
       (select array_agg(pl.user_id) from project_leads pl where pl.project_id = data.project_id) as project_lead_ids

FROM (SELECT d.project_id                           as project_id,
             d.timestamp                            as timestamp,
             d.contributor_id                       as contributor_id,
             d.search                               as search,
             coalesce(d.language_ids, '{}')         as language_ids,
             coalesce(d.ecosystem_ids, '{}')        as ecosystem_ids,
             coalesce(d.program_ids, '{}')          as program_ids,
             coalesce(d.project_category_ids, '{}') as project_category_ids,
             d.contribution_id                      as contribution_id,
             d.is_merged_pr                         as is_merged_pr,
             d.is_first_contribution_on_onlydust    as is_first_contribution_on_onlydust,
             NULL::uuid                             as reward_id,
             NULL::numeric                          as rewarded_usd_amount,
             NULL::numeric                          as granted_usd_amount
      from bi.contribution_data_cross_projects d

      UNION

      SELECT d.project_id                           as project_id,
             d.timestamp                            as timestamp,
             d.contributor_id                       as contributor_id,
             d.search                               as search,
             coalesce(d.language_ids, '{}')         as language_ids,
             coalesce(d.ecosystem_ids, '{}')        as ecosystem_ids,
             coalesce(d.program_ids, '{}')          as program_ids,
             coalesce(d.project_category_ids, '{}') as project_category_ids,
             NULL                                   as contribution_id,
             NULL                                   as is_merged_pr,
             NULL                                   as is_first_contribution_on_onlydust,
             d.reward_id                            as reward_id,
             d.usd_amount                           as rewarded_usd_amount,
             NULL                                   as granted_usd_amount
      from bi.reward_data d

      UNION

      SELECT d.project_id         as project_id,
             d.day_timestamp      as timestamp,
             NULL                 as contributor_id,
             d.search             as search,
             '{}'                 as language_ids,
             '{}'                 as ecosystem_ids,
             array [d.program_id] as program_ids,
             '{}'                 as project_category_ids,
             NULL                 as contribution_id,
             NULL                 as is_merged_pr,
             NULL                 as is_first_contribution_on_onlydust,
             NULL                 as reward_id,
             NULL                 as rewarded_usd_amount,
             d.usd_amount         as granted_usd_amount
      from bi.daily_project_grants d) data;

CREATE UNIQUE INDEX bi_project_data_unions_pk ON bi.project_data_unions (project_id, timestamp, contribution_id, reward_id, granted_usd_amount);



CREATE MATERIALIZED VIEW bi.project_global_data AS
SELECT *,
       p.name                                       as project_name,
       jsonb_build_object('id', p.id,
                          'slug', p.slug,
                          'name', p.name,
                          'logoUrl', p.logo_url)    as project,

       (select jsonb_agg(jsonb_build_object('id', u.id,
                                            'login', u.github_login,
                                            'githubUserId', u.github_user_id,
                                            'avatarUrl', u.github_avatar_url))
        from iam.users u
        where u.id = any (project_lead_ids))        as leads,

       (select jsonb_agg(jsonb_build_object('id', pc.id,
                                            'slug', pc.slug,
                                            'name', pc.name,
                                            'description', pc.description,
                                            'iconSlug', pc.icon_slug))
        from project_categories pc
        where pc.id = any (d.project_category_ids)) as categories,

       (select jsonb_agg(jsonb_build_object('id', l.id,
                                            'slug', l.slug,
                                            'name', l.name,
                                            'logoUrl', l.logo_url,
                                            'bannerUrl', l.banner_url))
        from languages l
        where l.id = any (d.language_ids))          as languages,

       (select jsonb_agg(jsonb_build_object('id', e.id,
                                            'slug', e.slug,
                                            'name', e.name,
                                            'logoUrl', e.logo_url,
                                            'bannerUrl', e.banner_url,
                                            'url', e.url))
        from ecosystems e
        where e.id = any (d.ecosystem_ids))         as ecosystems,

       (select jsonb_agg(jsonb_build_object('id', p.id,
                                            'name', p.name,
                                            'logoUrl', p.logo_url))
        from programs p
        where p.id = any (d.program_ids))           as programs

FROM (SELECT d.project_id,
             array_uniq_cat_agg(d.project_lead_ids)     as project_lead_ids,
             array_uniq_cat_agg(d.project_category_ids) as project_category_ids,
             array_uniq_cat_agg(d.language_ids)         as language_ids,
             array_uniq_cat_agg(d.ecosystem_ids)        as ecosystem_ids,
             array_uniq_cat_agg(d.program_ids)          as program_ids
      FROM bi.project_data_unions d
      GROUP BY d.project_id) d
         JOIN projects p ON p.id = d.project_id;

CREATE UNIQUE INDEX bi_project_global_data_pk ON bi.project_global_data (project_id);

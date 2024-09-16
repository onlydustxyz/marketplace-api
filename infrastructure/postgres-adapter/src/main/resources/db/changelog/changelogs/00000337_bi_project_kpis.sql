DROP MATERIALIZED VIEW bi.contribution_data_cross_projects;

CREATE MATERIALIZED VIEW bi.contribution_data_cross_projects AS
select *,

       (select max(previous.timestamp) as timestamp
        from bi_internal.contribution_project_timestamps previous
        where previous.project_id = c.project_id
          and previous.timestamp < c.timestamp) as previous_project_contribution_timestamp,

       (select min(next.timestamp) as timestamp
        from bi_internal.contribution_project_timestamps next
        where next.project_id = c.project_id
          and next.timestamp > c.timestamp)     as next_project_contribution_timestamp

from (select c.contribution_id,
             projects.id as project_id,
             c.contributor_id,
             c.contributor_user_id,
             c.contributor_country,
             c.timestamp,
             c.language_ids,
             c.ecosystem_ids,
             c.program_ids,
             c.project_category_ids,
             c.is_merged_pr,
             c.is_first_contribution_on_onlydust
      from bi.contribution_data c
               CROSS JOIN unnest(c.project_ids) AS projects(id)
      where projects.id is not null) c;

create unique index bi_cdcp_pk on bi.contribution_data_cross_projects (contribution_id, project_id);
create index bi_cdcp_timestamp_program_ids_ecosystem_ids_idx on bi.contribution_data_cross_projects (timestamp, program_ids, ecosystem_ids);



CREATE MATERIALIZED VIEW bi.project_data_unions AS
SELECT *,
       (select array_agg(pl.user_id) from project_leads pl where pl.project_id = data.project_id) as project_lead_ids

FROM (SELECT d.project_id                           as project_id,
             d.timestamp                            as timestamp,
             d.contributor_id                       as contributor_id,
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



CREATE OR REPLACE FUNCTION array_uniq_cat_agg(anyarray, anyarray)
    RETURNS anyarray
    LANGUAGE SQL AS
$$
SELECT ARRAY(SELECT DISTINCT unnest($1 || $2))
$$;


CREATE AGGREGATE array_uniq_cat_agg(anyarray) (
    SFUNC = array_uniq_cat_agg,
    STYPE = anyarray,
    INITCOND = '{}'
    );



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
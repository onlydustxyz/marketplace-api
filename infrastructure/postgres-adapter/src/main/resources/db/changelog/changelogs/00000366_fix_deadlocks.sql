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

    -- NOTE: to avoid deadlocks, rows are properly locked before being deleted.
    -- If a row is already locked, IT WILL BE SKIPPED as it is already being updated by another refresh.
    EXECUTE format(
            'delete from %I.%I where %I in ( select %I from %I.%I where not exists(select 1 from %I.%I m where m.%I = %I.%I and m.hash = %I.hash) for update skip locked )',
            schema, projection_table_name, pk_name,
            pk_name, schema, projection_table_name,
            schema, materialized_view_name, pk_name, projection_table_name, pk_name, projection_table_name);

    EXECUTE format('insert into %I.%I select * from %I.%I m where not exists(select 1 from %I.%I p where p.%I = m.%I) order by %I on conflict (%I) do nothing',
                   schema, projection_table_name, schema, materialized_view_name, schema, projection_table_name, pk_name, pk_name, pk_name, pk_name);
END
$$;



CREATE OR REPLACE PROCEDURE refresh_pseudo_projection_unsafe(schema text, name text, pk_name text, condition text)
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

    EXECUTE format('with to_insert as materialized (select * from %I.%I v where %s)' ||
                   'insert into %I.%I select * from to_insert where not exists(select 1 from %I.%I p where p.%I = to_insert.%I) order by %I on conflict (%I) do nothing',
                   schema, view_name, condition,
                   schema, projection_table_name, schema, projection_table_name, pk_name, pk_name, pk_name, pk_name);
END
$$;



CREATE OR REPLACE VIEW bi.v_contribution_data AS
SELECT v.*, md5(v::text) as hash
FROM (with project_contributions AS (select distinct on (c.id) c.*,
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
             string_agg(distinct lfe.name, ' ')                                                               as languages,
             bool_or(gl.name ~~* '%good%first%issue%')                                                        as is_good_first_issue,
             array_agg(distinct gia.user_id) filter ( where gia.user_id is not null )                         as assignee_ids
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
               left join indexer_exp.github_issues_labels gil ON gil.issue_id = c.issue_id
               left join indexer_exp.github_labels gl ON gil.label_id = gl.id
               left join indexer_exp.github_issues_assignees gia ON gia.issue_id = c.issue_id
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
               ru.country) v;

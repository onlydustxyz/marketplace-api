CREATE OR REPLACE FUNCTION get_pseudo_projection_definition(schema text, name text)
    RETURNS text AS
$$
DECLARE
    view_name                    text;
    pseudo_projection_definition text;
BEGIN
    view_name := 'v_' || name;
    SELECT regexp_replace(pg_get_viewdef(format('%I.%I', schema, view_name)::regclass, true),
                          '.*md5\(v\.\*::text\)\s*AS\s*hash\s*FROM\s*\(\s*(.+)\s*\)\s*v.*', '\1', 'i')
    INTO pseudo_projection_definition;
    RETURN pseudo_projection_definition;
END
$$
    LANGUAGE plpgsql;



create schema migration;
create table migration.pseudo_projection_definitions
(
    schema_name            text,
    pseudo_projection_name text,
    definition             text,
    primary key (schema_name, pseudo_projection_name)
);



create or replace procedure drop_pseudo_projection(IN schema text, IN name text)
    language plpgsql
as
$$
DECLARE
    view_name              text;
    materialized_view_name text;
    projection_table_name  text;
BEGIN
    view_name := 'v_' || name;
    materialized_view_name := 'm_' || name;
    projection_table_name := 'p_' || name;

    INSERT INTO migration.pseudo_projection_definitions (schema_name, pseudo_projection_name, definition)
    SELECT schema, name, get_pseudo_projection_definition(schema, name)
    ON CONFLICT (schema_name, pseudo_projection_name) DO UPDATE SET definition = EXCLUDED.definition;

    EXECUTE format('DROP TABLE %I.%I', schema, projection_table_name);
    EXECUTE format('DROP MATERIALIZED VIEW %I.%I', schema, materialized_view_name);
    EXECUTE format('DROP VIEW %I.%I', schema, view_name);
END
$$;



CREATE OR REPLACE PROCEDURE restore_pseudo_projection(IN schema text, IN name text, IN pk_name text)
    LANGUAGE plpgsql
AS
$$
DECLARE
    pseudo_projection_definition text;
BEGIN
    SELECT definition
    INTO pseudo_projection_definition
    FROM migration.pseudo_projection_definitions
    WHERE schema_name = schema
      AND pseudo_projection_name = name;

    IF pseudo_projection_definition IS NULL THEN
        RAISE EXCEPTION 'Definition not found for pseudo-projection %.%', schema, name;
    END IF;

    CALL create_pseudo_projection(schema, name, pseudo_projection_definition, pk_name);
END
$$;



call drop_pseudo_projection('bi', 'application_data');
call drop_pseudo_projection('bi', 'project_contributions_data');
call drop_pseudo_projection('bi', 'per_contributor_contribution_data');



call drop_pseudo_projection('bi', 'contribution_contributors_data');
call create_pseudo_projection('bi', 'contribution_contributors_data', $$
select c.contribution_uuid                                                                    as contribution_uuid,
       c.repo_id                                                                              as repo_id,
       c.github_author_id                                                                     as github_author_id,
       array_agg(distinct gcc.contributor_id) filter ( where gcc.contributor_id is not null ) as contributor_ids,
       array_agg(distinct gia.user_id) filter ( where gia.user_id is not null )               as assignee_ids,
       array_agg(distinct a.applicant_id) filter ( where a.applicant_id is not null )         as applicant_ids,

       case when ad.contributor_id is not null then ad.contributor end                        as github_author,
       case when mbd.contributor_id is not null then mbd.contributor end                      as github_merged_by,

       jsonb_agg(distinct jsonb_set(jsonb_set(cd.contributor, '{since}', to_jsonb(gcc.tech_created_at::timestamptz), true),
                                    '{assignedBy}', coalesce(assigned_by_d.contributor, '{}'::jsonb), assigned_by_d.contributor is not null))
       filter ( where cd.contributor_id is not null )                                         as contributors,

       jsonb_agg(distinct jsonb_set(jsonb_set(apd.contributor, '{since}', to_jsonb(a.received_at::timestamptz), true),
                                    '{applicationId}', to_jsonb(a.id), true))
       filter ( where apd.contributor_id is not null )                                        as applicants,

       concat(c.github_number, ' ',
              c.github_title, ' ',
              ad.contributor_login, ' ',
              string_agg(distinct cd.contributor_login, ' '), ' ',
              string_agg(distinct apd.contributor_login, ' ')
       )                                                                                      as search
from indexer_exp.grouped_contributions c
         left join indexer_exp.grouped_contribution_contributors gcc on gcc.contribution_uuid = c.contribution_uuid
         left join bi.p_contributor_global_data cd on cd.contributor_id = gcc.contributor_id
         left join bi.p_contributor_global_data ad on ad.contributor_id = c.github_author_id
         left join indexer_exp.github_pull_requests gpr ON gpr.id = c.pull_request_id
         left join bi.p_contributor_global_data mbd on mbd.contributor_id = gpr.merged_by_id
         left join indexer_exp.github_issues_assignees gia ON gia.issue_id = c.issue_id
         left join bi.p_contributor_global_data assigned_by_d on assigned_by_d.contributor_id = gia.assigned_by_user_id
         left join applications a on a.issue_id = c.issue_id
         left join bi.p_contributor_global_data apd on apd.contributor_id = a.applicant_id
group by c.contribution_uuid, ad.contributor_id, mbd.contributor_id
$$, 'contribution_uuid');



call restore_pseudo_projection('bi', 'per_contributor_contribution_data', 'technical_id');
call restore_pseudo_projection('bi', 'project_contributions_data', 'project_id');
call restore_pseudo_projection('bi', 'application_data', 'application_id');

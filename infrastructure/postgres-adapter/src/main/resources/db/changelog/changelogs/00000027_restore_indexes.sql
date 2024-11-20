create unique index if not exists bi_p_project_contributions_data_pid_ccount_gficount_index on bi.p_project_contributions_data (project_id, contributor_count, good_first_issue_count);
create index if not exists bi_p_project_contributions_data_repo_ids_index on bi.p_project_contributions_data using gin (repo_ids);

create index if not exists p_reward_data_contributor_id_project_id_index
    on bi.p_reward_data (contributor_id, project_id);

create index if not exists p_application_data_contributor_id_project_id_index
    on bi.p_application_data (contributor_id, project_id);

create index if not exists p_contributor_global_data_login_id_index
    on bi.p_contributor_global_data (contributor_login, contributor_id);

create index if not exists bi_p_contribution_contributors_data_applicant_ids_index on bi.p_contribution_contributors_data using gin (applicant_ids);
create index if not exists bi_p_contribution_contributors_data_repo_id_index on bi.p_contribution_contributors_data (repo_id);
create index if not exists bi_p_contribution_contributors_data_contributor_ids_index on bi.p_contribution_contributors_data using gin (contributor_ids);

create index if not exists bi_p_application_data_contribution_uuid_index on bi.p_application_data (contribution_uuid);
create index if not exists bi_p_application_data_repo_id_index on bi.p_application_data (repo_id);
create unique index if not exists p_application_data_contributor_id_status_application_id_uindex
    on bi.p_application_data (contributor_id, status, application_id);

create index if not exists bi_p_application_data_project_id_timestamp_idx on bi.p_application_data (project_id, timestamp);
create index if not exists bi_p_application_data_project_id_day_timestamp_idx on bi.p_application_data (project_id, day_timestamp);
create index if not exists bi_p_application_data_project_id_week_timestamp_idx on bi.p_application_data (project_id, week_timestamp);
create index if not exists bi_p_application_data_project_id_month_timestamp_idx on bi.p_application_data (project_id, month_timestamp);
create index if not exists bi_p_application_data_project_id_quarter_timestamp_idx on bi.p_application_data (project_id, quarter_timestamp);
create index if not exists bi_p_application_data_project_id_year_timestamp_idx on bi.p_application_data (project_id, year_timestamp);
create index if not exists bi_p_application_data_project_id_timestamp_idx_inv on bi.p_application_data (timestamp, project_id);
create index if not exists bi_p_application_data_project_id_day_timestamp_idx_inv on bi.p_application_data (day_timestamp, project_id);
create index if not exists bi_p_application_data_project_id_week_timestamp_idx_inv on bi.p_application_data (week_timestamp, project_id);
create index if not exists bi_p_application_data_project_id_month_timestamp_idx_inv on bi.p_application_data (month_timestamp, project_id);
create index if not exists bi_p_application_data_project_id_quarter_timestamp_idx_inv on bi.p_application_data (quarter_timestamp, project_id);
create index if not exists bi_p_application_data_project_id_year_timestamp_idx_inv on bi.p_application_data (year_timestamp, project_id);

create index if not exists bi_p_application_data_contributor_id_timestamp_idx on bi.p_application_data (contributor_id, timestamp);
create index if not exists bi_p_application_data_contributor_id_day_timestamp_idx on bi.p_application_data (contributor_id, day_timestamp);
create index if not exists bi_p_application_data_contributor_id_week_timestamp_idx on bi.p_application_data (contributor_id, week_timestamp);
create index if not exists bi_p_application_data_contributor_id_month_timestamp_idx on bi.p_application_data (contributor_id, month_timestamp);
create index if not exists bi_p_application_data_contributor_id_quarter_timestamp_idx on bi.p_application_data (contributor_id, quarter_timestamp);
create index if not exists bi_p_application_data_contributor_id_year_timestamp_idx on bi.p_application_data (contributor_id, year_timestamp);
create index if not exists bi_p_application_data_contributor_id_timestamp_idx_inv on bi.p_application_data (timestamp, contributor_id);
create index if not exists bi_p_application_data_contributor_id_day_timestamp_idx_inv on bi.p_application_data (day_timestamp, contributor_id);
create index if not exists bi_p_application_data_contributor_id_week_timestamp_idx_inv on bi.p_application_data (week_timestamp, contributor_id);
create index if not exists bi_p_application_data_contributor_id_month_timestamp_idx_inv on bi.p_application_data (month_timestamp, contributor_id);
create index if not exists bi_p_application_data_contributor_id_quarter_timestamp_idx_inv on bi.p_application_data (quarter_timestamp, contributor_id);
create index if not exists bi_p_application_data_contributor_id_year_timestamp_idx_inv on bi.p_application_data (year_timestamp, contributor_id);



create index if not exists bi_p_per_contributor_contribution_data_contribution_uuid_index on bi.p_per_contributor_contribution_data (contribution_uuid);
create index if not exists bi_p_per_contributor_contribution_data_project_id_index on bi.p_per_contributor_contribution_data (project_id);
create index if not exists bi_p_per_contributor_contribution_data_project_slug_index on bi.p_per_contributor_contribution_data (project_slug);
create index if not exists bi_p_per_contributor_contribution_data_cid_pid_timestamp_index on bi.p_per_contributor_contribution_data (contributor_id, project_id, timestamp desc);
create unique index if not exists bi_p_per_contributor_contribution_data_cid_cuuid_index on bi.p_per_contributor_contribution_data (contributor_id, contribution_uuid);
create unique index if not exists bi_p_per_contributor_contribution_data_cuserid_cuuid_index on bi.p_per_contributor_contribution_data (contributor_user_id, contribution_uuid);

create index if not exists bi_contribution_data_repo_id_idx on bi.p_per_contributor_contribution_data (repo_id);

create index if not exists bi_contribution_data_project_id_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, timestamp);
create index if not exists bi_contribution_data_project_id_day_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, day_timestamp);
create index if not exists bi_contribution_data_project_id_week_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, week_timestamp);
create index if not exists bi_contribution_data_project_id_month_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, month_timestamp);
create index if not exists bi_contribution_data_project_id_quarter_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, quarter_timestamp);
create index if not exists bi_contribution_data_project_id_year_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, year_timestamp);
create index if not exists bi_contribution_data_project_id_timestamp_idx_inv on bi.p_per_contributor_contribution_data (timestamp, project_id);
create index if not exists bi_contribution_data_project_id_day_timestamp_idx_inv on bi.p_per_contributor_contribution_data (day_timestamp, project_id);
create index if not exists bi_contribution_data_project_id_week_timestamp_idx_inv on bi.p_per_contributor_contribution_data (week_timestamp, project_id);
create index if not exists bi_contribution_data_project_id_month_timestamp_idx_inv on bi.p_per_contributor_contribution_data (month_timestamp, project_id);
create index if not exists bi_contribution_data_project_id_quarter_timestamp_idx_inv on bi.p_per_contributor_contribution_data (quarter_timestamp, project_id);

create index if not exists bi_contribution_data_project_id_year_timestamp_idx_inv on bi.p_per_contributor_contribution_data (year_timestamp, project_id);
create index if not exists bi_contribution_data_contributor_id_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, timestamp);
create index if not exists bi_contribution_data_contributor_id_day_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, day_timestamp);
create index if not exists bi_contribution_data_contributor_id_week_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, week_timestamp);
create index if not exists bi_contribution_data_contributor_id_month_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, month_timestamp);
create index if not exists bi_contribution_data_contributor_id_quarter_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, quarter_timestamp);
create index if not exists bi_contribution_data_contributor_id_year_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, year_timestamp);
create index if not exists bi_contribution_data_contributor_id_timestamp_idx_inv on bi.p_per_contributor_contribution_data (timestamp, contributor_id);
create index if not exists bi_contribution_data_contributor_id_day_timestamp_idx_inv on bi.p_per_contributor_contribution_data (day_timestamp, contributor_id);
create index if not exists bi_contribution_data_contributor_id_week_timestamp_idx_inv on bi.p_per_contributor_contribution_data (week_timestamp, contributor_id);
create index if not exists bi_contribution_data_contributor_id_month_timestamp_idx_inv on bi.p_per_contributor_contribution_data (month_timestamp, contributor_id);
create index if not exists bi_contribution_data_contributor_id_quarter_timestamp_idx_inv on bi.p_per_contributor_contribution_data (quarter_timestamp, contributor_id);

create index if not exists bi_contribution_data_contributor_id_year_timestamp_idx_inv on bi.p_per_contributor_contribution_data (year_timestamp, contributor_id);



create table migration.pseudo_projection_indexes
(
    schema_name            text,
    pseudo_projection_name text,
    index_name             text,
    definition             text,
    primary key (schema_name, pseudo_projection_name, index_name)
);



CREATE OR REPLACE PROCEDURE save_pseudo_projection_definition(schema text, name text)
AS
$$
DECLARE
    view_name                    text;
    pseudo_projection_definition text;
BEGIN
    view_name := 'v_' || name;
    SELECT regexp_replace(pg_get_viewdef(format('%I.%I', schema, view_name)::regclass, true),
                          '.*md5\(v\.\*::text\)\s*AS\s*hash\s*FROM\s*\(\s*(.+)\s*\)\s*v.*', '\1', 'i')
    INTO pseudo_projection_definition;

    INSERT INTO migration.pseudo_projection_definitions (schema_name, pseudo_projection_name, definition)
    SELECT schema, name, pseudo_projection_definition
    ON CONFLICT (schema_name, pseudo_projection_name) DO UPDATE SET definition = EXCLUDED.definition;

END
$$
    LANGUAGE plpgsql;



CREATE OR REPLACE PROCEDURE save_pseudo_projection_indexes_definition(schema text, name text)
AS
$$
DECLARE
    projection_table_name text;
    index_record          record;
BEGIN
    projection_table_name := 'p_' || name;
    FOR index_record IN
        SELECT indexname, indexdef
        FROM pg_indexes
        WHERE schemaname = schema
          AND tablename = projection_table_name
        LOOP
            INSERT INTO migration.pseudo_projection_indexes (schema_name, pseudo_projection_name, index_name, definition)
            VALUES (schema, name, index_record.indexname, index_record.indexdef)
            ON CONFLICT (schema_name, pseudo_projection_name, index_name) DO UPDATE
                SET definition = EXCLUDED.definition;
        END LOOP;
END
$$
    LANGUAGE plpgsql;



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

    CALL save_pseudo_projection_definition(schema, name);
    CALL save_pseudo_projection_indexes_definition(schema, name);
    EXECUTE format('DROP TABLE %I.%I', schema, projection_table_name);
    EXECUTE format('DROP MATERIALIZED VIEW %I.%I', schema, materialized_view_name);
    EXECUTE format('DROP VIEW %I.%I', schema, view_name);
END
$$;



CREATE OR REPLACE PROCEDURE restore_pseudo_projection_indexes(IN schema text, IN name text)
    LANGUAGE plpgsql
AS
$$
DECLARE
    index_record          RECORD;
    index_exists          boolean;
    projection_table_name text;
BEGIN
    projection_table_name := 'p_' || name;
    FOR index_record IN
        SELECT index_name, definition
        FROM migration.pseudo_projection_indexes
        WHERE schema_name = schema
          AND pseudo_projection_name = name
        LOOP
            SELECT EXISTS (SELECT 1
                           FROM pg_indexes
                           WHERE schemaname = schema
                             AND tablename = projection_table_name
                             AND indexname = index_record.index_name)
            INTO index_exists;

            IF NOT index_exists THEN
                EXECUTE index_record.definition;
            END IF;
        END LOOP;
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
    CALL restore_pseudo_projection_indexes(schema, name);
END
$$;


-- TEST ---------------------------------------------------------------------------------------------
create schema tmp_test;
call create_pseudo_projection('tmp_test', 'my_projection',
                              $$SELECT * FROM (VALUES ('Foo', 25), ('Bar', 30), ('Baz', 35)) AS t(name, age)$$, 'name');

create index tmp_test_my_projection_age_index on tmp_test.p_my_projection (age);

call drop_pseudo_projection('tmp_test', 'my_projection');

DO
$$
    DECLARE
        definition_exists boolean;
    BEGIN
        SELECT EXISTS (SELECT 1
                       FROM migration.pseudo_projection_definitions
                       WHERE schema_name = 'tmp_test'
                         AND pseudo_projection_name = 'my_projection')
        INTO definition_exists;
        IF NOT definition_exists THEN
            RAISE EXCEPTION 'Definition for tmp_test.my_projection not found in migration.pseudo_projection_definitions';
        END IF;

        SELECT EXISTS (SELECT 1
                       FROM migration.pseudo_projection_indexes
                       WHERE schema_name = 'tmp_test'
                         AND pseudo_projection_name = 'my_projection'
                         AND index_name = 'tmp_test_my_projection_age_index')
        INTO definition_exists;
        IF NOT definition_exists THEN
            RAISE EXCEPTION 'Definition for tmp_test_my_projection_age_index not found in migration.pseudo_projection_indexes';
        END IF;
    END
$$;

call restore_pseudo_projection('tmp_test', 'my_projection', 'name');

DO
$$
    DECLARE
        index_exists boolean;
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM tmp_test.p_my_projection WHERE name = 'Foo') THEN
            RAISE EXCEPTION 'Value Foo not found in tmp_test.p_my_projection';
        END IF;
        SELECT EXISTS (SELECT 1
                       FROM pg_indexes
                       WHERE schemaname = 'tmp_test'
                         AND tablename = 'p_my_projection'
                         AND indexname = 'tmp_test_my_projection_age_index'
                         AND indexdef = 'CREATE INDEX tmp_test_my_projection_age_index ON tmp_test.p_my_projection USING btree (age)')
        INTO index_exists;
        IF NOT index_exists THEN
            RAISE EXCEPTION 'Definition for tmp_test_my_projection_age_index not found in migration.pseudo_projection_indexes';
        END IF;
    END
$$;
DROP SCHEMA IF EXISTS auth;
DROP TABLE IF EXISTS public.__diesel_schema_migrations;



CREATE TABLE view_definitions
(
    schema_name text,
    view_name   text,
    definition  text
);



CREATE OR REPLACE FUNCTION drop_and_save_views(target_schema text)
    RETURNS void AS
$$
DECLARE
    view_record record;
BEGIN
    FOR view_record IN
        SELECT schemaname, viewname, definition
        FROM pg_views
        WHERE schemaname = target_schema
        LOOP
            -- Save the view definition
            INSERT INTO view_definitions (schema_name, view_name, definition)
            VALUES (view_record.schemaname, view_record.viewname, view_record.definition);

            -- Drop the view
            EXECUTE format('DROP VIEW IF EXISTS %I.%I CASCADE',
                           view_record.schemaname, view_record.viewname);
        END LOOP;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION create_views_from_definitions()
    RETURNS void AS
$$
DECLARE
    view_record record;
BEGIN
    FOR view_record IN
        SELECT schema_name, view_name, definition
        FROM view_definitions
        LOOP
            EXECUTE format('CREATE OR REPLACE VIEW %I.%I AS %s',
                           view_record.schema_name,
                           view_record.view_name,
                           view_record.definition);
        END LOOP;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION convert_timestamps_to_timestamptz(schema_name text)
    RETURNS void AS
$$
DECLARE
    table_name  text;
    column_name text;
BEGIN
    FOR table_name, column_name IN
        SELECT c.table_name, c.column_name
        FROM information_schema.columns c
        WHERE c.table_schema = schema_name
          AND c.data_type = 'timestamp without time zone'
        LOOP
            EXECUTE format('ALTER TABLE %I.%I ALTER COLUMN %I TYPE timestamptz USING %I AT TIME ZONE ''UTC''',
                           schema_name, table_name, column_name, column_name);
        END LOOP;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION nuke_timestamps(schema_name text)
    RETURNS void AS
$$
BEGIN
    PERFORM drop_and_save_views(schema_name);
    PERFORM convert_timestamps_to_timestamptz(schema_name);
    RAISE NOTICE 'Schema % processed: timestamps converted, views dropped and saved', schema_name;
END;
$$ LANGUAGE plpgsql;



SELECT nuke_timestamps('accounting');
SELECT create_views_from_definitions();


DROP FUNCTION drop_and_save_views;
DROP FUNCTION convert_timestamps_to_timestamptz;
DROP FUNCTION create_views_from_definitions;
DROP FUNCTION nuke_timestamps;

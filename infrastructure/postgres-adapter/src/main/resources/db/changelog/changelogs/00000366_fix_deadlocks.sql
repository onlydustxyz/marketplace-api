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

    EXECUTE format('insert into %I.%I select * from %I.%I order by %I on conflict (%I) do nothing',
                   schema, projection_table_name, schema, materialized_view_name, pk_name, pk_name);
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

    EXECUTE format('insert into %I.%I select * from %I.%I where %s order by %I on conflict (%I) do nothing',
                   schema, projection_table_name, schema, view_name, condition, pk_name, pk_name);
END
$$;

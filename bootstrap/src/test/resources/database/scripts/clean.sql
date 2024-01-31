CREATE OR REPLACE FUNCTION truncate_tables(schema_name IN VARCHAR) RETURNS void AS
$$
DECLARE
    statements CURSOR FOR
        SELECT tablename
        FROM pg_tables
        WHERE schemaname = schema_name;
BEGIN
    FOR stmt IN statements
        LOOP
            EXECUTE 'TRUNCATE TABLE ' || schema_name || '.' || quote_ident(stmt.tablename) || ' RESTART IDENTITY CASCADE;';
        END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT truncate_tables('public');
SELECT truncate_tables('iam');
SELECT truncate_tables('rfd');

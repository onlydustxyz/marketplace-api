CREATE OR REPLACE FUNCTION sum_func(
    bigint, pg_catalog.anyelement, bigint
)
    RETURNS bigint AS
$body$
SELECT case when $3 is not null then COALESCE($1, 0) + $3 else $1 end
$body$
    STABLE
    PARALLEL RESTRICTED
    LANGUAGE 'sql';
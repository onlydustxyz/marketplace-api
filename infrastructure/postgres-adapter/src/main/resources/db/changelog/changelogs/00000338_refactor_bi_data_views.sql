create index bi_contribution_data_contributor_id_timestamp_idx on bi.contribution_data (contributor_id, timestamp);



CREATE OR REPLACE FUNCTION create_materialized_view_contributor_data(time_trunc text) RETURNS void
    LANGUAGE plpgsql AS
$func$
BEGIN
    EXECUTE format('CREATE MATERIALIZED VIEW bi.%s_contributor_data AS
select c.contributor_id                    as contributor_id,
       date_trunc(''%s'', c.timestamp)     as timestamp,
       array_uniq_cat_agg(c.ecosystem_ids) as ecosystem_ids,
       array_uniq_cat_agg(c.program_ids)   as program_ids
from bi.contribution_data c
group by 1, 2;
create unique index bi_%s_contributor_data_pk on bi.%s_contributor_data (contributor_id, timestamp);
create unique index bi_%s_contributor_data_pk_inv on bi.%s_contributor_data (timestamp, contributor_id);',
                   time_trunc, time_trunc, time_trunc, time_trunc, time_trunc, time_trunc);
    RETURN;
END
$func$;

select create_materialized_view_contributor_data('day');
select create_materialized_view_contributor_data('week');
select create_materialized_view_contributor_data('month');
select create_materialized_view_contributor_data('quarter');
select create_materialized_view_contributor_data('year');
DROP FUNCTION create_materialized_view_contributor_data(time_trunc text);

INSERT INTO accounting.all_transactions (id, timestamp, type, currency_id, amount, sponsor_id, program_id, project_id, reward_id, payment_id, deposit_status)
SELECT d.id,
       tt.timestamp,
       'DEPOSIT',
       d.currency_id,
       tt.amount,
       d.sponsor_id,
       null,
       null,
       null,
       null,
       d.status
FROM accounting.deposits d
         join accounting.transfer_transactions tt on tt.id = d.transaction_id
where d.status != 'DRAFT';



CREATE OR REPLACE PROCEDURE refresh_pseudo_projection(schema text, name text, params jsonb)
    LANGUAGE plpgsql
AS
$$
DECLARE
    view_name             text;
    projection_table_name text;
    condition             text;
    key                   text;
    value                 text;
BEGIN
    view_name := 'v_' || name;
    projection_table_name := 'p_' || name;

    condition := '';
    FOR key, value IN SELECT * FROM jsonb_each_text(params)
        LOOP
            condition := condition || format(' and %I = %L', key, value);
        END LOOP;

    EXECUTE format('delete from %I.%I where true %s', schema, projection_table_name, condition);

    EXECUTE format('insert into %I.%I select * from %I.%I where true %s on conflict do nothing', schema, projection_table_name, schema, view_name,
                   condition);
END
$$;
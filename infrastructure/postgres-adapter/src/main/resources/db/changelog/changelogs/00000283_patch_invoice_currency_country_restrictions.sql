create or replace function update_array_elements(arr jsonb, path text[])
    returns jsonb
    language sql as
$$
select jsonb_agg(jsonb_set(e, path || '{countryRestrictions}',
                           case
                               when e #>> (path || '{code}') = 'STRK' then '[
                                   "USA"
                               ]'::jsonb
                               else '[]'::jsonb
                               end,
                           true))
from jsonb_array_elements(arr) e(e)
$$;

update accounting.invoices
set data = jsonb_set(data, '{rewards}', update_array_elements(data -> 'rewards', '{currency}'));

update accounting.invoices
set data = jsonb_set(data, '{rewards}', update_array_elements(data -> 'rewards', '{targetCurrency}'));

drop function update_array_elements(jsonb, text[]);

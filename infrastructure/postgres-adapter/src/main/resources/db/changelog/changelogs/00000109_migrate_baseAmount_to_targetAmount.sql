create or replace function update_array_elements(arr jsonb, old_key text[], new_key text[])
    returns jsonb
    language sql as
$$
select jsonb_agg(jsonb_set(e, new_key, e #> old_key, true) #- old_key)
from jsonb_array_elements(arr) e(e)
$$;

update accounting.invoices
set data = jsonb_set(data, '{rewards}', update_array_elements(data -> 'rewards', '{baseAmount}', '{targetAmount}'));

drop function update_array_elements(jsonb, text[], text[]);
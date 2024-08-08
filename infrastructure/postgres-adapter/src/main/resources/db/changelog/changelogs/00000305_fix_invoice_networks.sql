create or replace function upper_all_elements(networks jsonb)
    returns jsonb
    language sql as
$$
select jsonb_agg(upper(e::text)::jsonb)
from jsonb_array_elements(networks) e(e)
$$;

create or replace function update_erc20_blockchain(erc20 jsonb)
    returns jsonb
    language sql as
$$
select coalesce(jsonb_agg(jsonb_set(e, '{blockchain}', to_jsonb(upper(e ->> 'blockchain')), false)), '[]')
from jsonb_array_elements(erc20) e(e)
$$;

create or replace function update_rewards(rewards jsonb)
    returns jsonb
    language sql as
$$
select jsonb_agg(
               jsonb_set(
                       jsonb_set(e, '{networks}', upper_all_elements(e -> 'networks'), false),
                       '{currency, erc20}',
                       update_erc20_blockchain(e #> '{currency, erc20}'),
                       false)
       )
from jsonb_array_elements(rewards) e(e)
$$;

update accounting.invoices
set data = jsonb_set(data, '{rewards}', update_rewards(data -> 'rewards'));

drop function update_rewards(jsonb);
drop function update_erc20_blockchain(jsonb);
drop function upper_all_elements(jsonb);

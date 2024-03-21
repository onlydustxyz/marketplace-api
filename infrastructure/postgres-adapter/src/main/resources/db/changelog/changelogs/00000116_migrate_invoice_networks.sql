create or replace function update_networks(arr jsonb)
    returns jsonb
    language sql as
$$
select jsonb_agg(jsonb_set(e, '{networks}'::text[], jsonb_build_array(
        case
            when e #>> '{currency,code}' = 'USD' THEN 'sepa'::accounting.network
            when e #>> '{currency,code}' = 'OP' THEN 'optimism'::accounting.network
            when e #>> '{currency,code}' = 'APT' THEN 'aptos'::accounting.network
            when e #>> '{currency,code}' = 'STRK' THEN 'starknet'::accounting.network
            else 'ethereum'::accounting.network
            end), true))
from jsonb_array_elements(arr) e(e)
$$;

update accounting.invoices
set data = jsonb_set(data, '{rewards}', update_networks(data -> 'rewards'));

drop function update_networks(jsonb);

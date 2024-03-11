-- CREATE sponsor as default value for projects with no sponsor
INSERT INTO sponsors(id, name, logo_url)
VALUES ('01bc5c57-9b7c-4521-b7be-8a12861ae5f4', 'No Sponsor',
        'https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp');

-- update missing sponsor in all budget allocations as first sponsor found
WITH budget_allocations AS (SELECT events.index                                  AS event_index,
                                   aggregate_id::UUID                            AS budget_id,
                                   (payload #>> '{Allocated, sponsor_id}')::UUID AS sponsor_id
                            FROM events
                            WHERE aggregate_name = 'BUDGET'
                              AND payload -> 'Allocated' IS NOT NULL)
UPDATE events
SET payload = jsonb_set(payload, '{Allocated, sponsor_id}',
                        to_jsonb(coalesce(ps.sponsor_id, '01bc5c57-9b7c-4521-b7be-8a12861ae5f4')),
                        TRUE)
FROM budget_allocations ba
         JOIN projects_budgets pb on pb.budget_id = ba.budget_id
         LEFT JOIN projects_sponsors ps ON ps.project_id = pb.project_id
where ba.event_index = index
  AND ba.sponsor_id IS NULL;

INSERT INTO erc20(blockchain, address, name, symbol, decimals, total_supply, currency_id)
SELECT 'optimism', '0x4200000000000000000000000000000000000042', 'Optimism', 'OP', 18, 4294967295989853760711675076, id
from currencies
where code = 'OP'
on conflict (blockchain, symbol)
    do nothing;

INSERT INTO erc20(blockchain, address, name, symbol, decimals, total_supply, currency_id)
SELECT 'starknet', '0x04718f5a0fc34cc1af16a1cdee98ffb20c31f5cd61d6ab07201858f4287c938d', 'StarkNet', 'STRK', 18, 570166196229945510490751569, id
from currencies
where code = 'STRK'
on conflict (blockchain, symbol)
    do nothing;
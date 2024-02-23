alter table rewards
    add column currency_id UUID not null references currencies (id);

drop view accounting.reward_statuses;

CREATE VIEW accounting.reward_statuses AS
WITH selected_billing_profile AS
         (SELECT ubpt.user_id,
                 ubpt.billing_profile_type = 'INDIVIDUAL' as is_individual,
                 CASE
                     WHEN ubpt.billing_profile_type = 'INDIVIDUAL' THEN ibp.verification_status = 'VERIFIED'
                     ELSE cbp.verification_status = 'VERIFIED'
                     END                                  as verified,
                 CASE
                     WHEN ubpt.billing_profile_type = 'INDIVIDUAL' THEN ibp.us_citizen
                     ELSE cbp.us_entity
                     END                                  as us_resident
          FROM user_billing_profile_types ubpt
                   LEFT JOIN individual_billing_profiles ibp on ibp.user_id = ubpt.user_id
                   LEFT JOIN company_billing_profiles cbp on cbp.user_id = ubpt.user_id),

     user_payout_networks AS
         (SELECT coalesce(w.user_id, ba.user_id)                                      AS user_id,
                 CASE WHEN w.user_id IS NOT NULL THEN array_agg(w.network) END ||
                 CASE WHEN ba.user_id IS NOT NULL THEN '{sepa, swift}'::network[] END AS networks
          FROM wallets w
                   FULL OUTER JOIN bank_accounts ba ON ba.user_id = w.user_id
          GROUP BY w.user_id, ba.user_id),

     yearly_usd_total_per_recipient AS
         (SELECT r.recipient_id,
                 SUM(rs.amount_usd_equivalent) as yearly_usd_total
          FROM public.rewards r
                   JOIN accounting.reward_status_data rs ON r.id = rs.reward_id AND rs.invoice_received_at IS NOT NULL
          WHERE rs.invoice_received_at >= date_trunc('year', now())
            AND rs.invoice_received_at < date_trunc('year', now()) + interval '1 year'
          GROUP BY r.recipient_id),

     aggregated_reward_status_data AS
         (SELECT reward_id,
                 bp.is_individual,
                 bp.verified                         as kycb_verified,
                 bp.us_resident                      as us_recipient,
                 c.code                              as reward_currency,
                 coalesce(yutpr.yearly_usd_total, 0) as current_year_usd_total,
                 rs.amount_usd_equivalent            as reward_usd_equivalent,
                 upn.networks @> rs.networks         as payout_info_filled,
                 rs.sponsor_has_enough_fund,
                 rs.unlock_date,
                 rs.invoice_received_at              as payment_requested_at,
                 rs.paid_at
          FROM accounting.reward_status_data rs
                   JOIN rewards r on r.id = rs.reward_id
                   JOIN currencies c on c.id = r.currency_id
                   LEFT JOIN iam.users u on u.github_user_id = r.recipient_id
                   LEFT JOIN selected_billing_profile bp on bp.user_id = u.id
                   LEFT JOIN user_payout_networks upn on upn.user_id = u.id
                   LEFT JOIN yearly_usd_total_per_recipient yutpr on yutpr.recipient_id = r.recipient_id)

SELECT reward_id,
       CASE
           WHEN s.paid_at IS NOT NULL THEN 'COMPLETE'::reward_status
           WHEN s.is_individual IS NULL THEN 'PENDING_BILLING_PROFILE'::reward_status
           WHEN s.kycb_verified IS NOT TRUE THEN 'PENDING_VERIFICATION'::reward_status
           WHEN s.us_recipient IS TRUE AND s.reward_currency = 'STRK' THEN 'PAYMENT_BLOCKED'::reward_status
           WHEN s.is_individual IS TRUE AND s.current_year_usd_total + s.reward_usd_equivalent > 5000 THEN 'PAYMENT_BLOCKED'::reward_status
           WHEN s.payout_info_filled IS NOT TRUE THEN 'PAYOUT_INFO_MISSING'::reward_status
           WHEN s.unlock_date IS NOT NULL AND s.unlock_date > NOW() THEN 'LOCKED'::reward_status
           WHEN s.sponsor_has_enough_fund IS NOT TRUE THEN 'LOCKED'::reward_status
           WHEN s.payment_requested_at IS NULL THEN 'PENDING_REQUEST'::reward_status
           ELSE 'PROCESSING'::reward_status
           END
           as status
FROM aggregated_reward_status_data s
;


DROP VIEW accounting.reward_usd_equivalent_data;


CREATE VIEW accounting.reward_usd_equivalent_data AS
WITH selected_billing_profile AS
         (SELECT ubpt.user_id,
                 CASE
                     WHEN ubpt.billing_profile_type = 'INDIVIDUAL' THEN ibp.updated_at
                     ELSE cbp.updated_at
                     END as verified_at
          FROM user_billing_profile_types ubpt
                   LEFT JOIN individual_billing_profiles ibp ON ibp.user_id = ubpt.user_id AND ibp.verification_status = 'VERIFIED'
                   LEFT JOIN company_billing_profiles cbp ON cbp.user_id = ubpt.user_id AND cbp.verification_status = 'VERIFIED'),

     currency_quote_available_at AS
         (SELECT hq.base_id, min(hq.timestamp) as available_at
          FROM accounting.historical_quotes hq
                   JOIN currencies usd ON usd.code = 'USD' AND usd.id = hq.target_id
          GROUP BY hq.base_id)

SELECT r.id             as reward_id,
       r.requested_at   as reward_created_at,
       r.currency_id    as reward_currency_id,
       bp.verified_at   as kycb_verified_at,
       cqa.available_at as currency_quote_available_at,
       rs.unlock_date   as unlock_date,
       r.amount         as reward_amount
FROM accounting.reward_status_data rs
         JOIN rewards r on r.id = rs.reward_id
         LEFT JOIN iam.users u on u.github_user_id = r.recipient_id
         LEFT JOIN selected_billing_profile bp on bp.user_id = u.id
         LEFT JOIN currency_quote_available_at cqa on cqa.base_id = r.currency_id;

alter table rewards
    drop column currency;


insert into public.currencies (id, type, name, code, logo_url, decimals, description, tech_created_at, tech_updated_at)
values ('17723f77-c108-4454-9cbe-80a7f931d879', 'FIAT', 'US Dollar', 'USD', null, 2, null, '2024-02-19 13:19:31.123403', '2024-02-19 13:19:31.123403'),
       ('91efcc9a-b62a-4bfc-88e4-dba8a75805a3', 'CRYPTO', 'Ethereum', 'ETH', 'https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png', 18,
        'Ethereum (ETH) is a cryptocurrency . Ethereum has a current supply of 120,165,512.39374194. The last known price of Ethereum is 2,910.3770179 USD and is up 4.15 over the last 24 hours. It is currently trading on 8424 active market(s) with $12,984,537,928.73 traded over the last 24 hours. More information can be found at https://www.ethereum.org/.',
        '2024-02-19 13:19:51.453793', '2024-02-19 13:19:51.453793'),
       ('83322c66-923a-4e09-8469-25295df7836a', 'CRYPTO', 'Aptos', 'APT', 'https://s2.coinmarketcap.com/static/img/coins/64x64/21794.png', 8,
        'Aptos (APT) is a cryptocurrency launched in 2022. Aptos has a current supply of 1,081,835,508.4322975 with 365,726,816.45748264 in circulation. The last known price of Aptos is 10.0941629 USD and is up 5.23 over the last 24 hours. It is currently trading on 332 active market(s) with $212,826,456.95 traded over the last 24 hours. More information can be found at https://aptosfoundation.org.',
        '2024-02-19 13:21:05.620584', '2024-02-19 13:21:05.620584'),
       ('7bb644bd-e2e1-437b-95f3-3dfff46a7918', 'CRYPTO', 'USD Coin', 'USDC', 'https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png', 6,
        'USDC (USDC) is a cryptocurrency and operates on the Ethereum platform. USDC has a current supply of 28,047,834,320.670963. The last known price of USDC is 1.0000048 USD and is down -0.01 over the last 24 hours. It is currently trading on 16756 active market(s) with $4,663,708,424.87 traded over the last 24 hours. More information can be found at https://www.centre.io/usdc.',
        '2024-02-19 13:21:42.214734', '2024-02-19 13:21:42.214734'),
       ('19083c20-13b3-4f66-b78a-f7f49f33a6c3', 'CRYPTO', 'Lords', 'LORDS', 'https://s2.coinmarketcap.com/static/img/coins/64x64/17445.png', 18,
        'LORDS (LORDS) is a cryptocurrency launched in 2021and operates on the Ethereum platform. LORDS has a current supply of 50,900,000 with 0 in circulation. The last known price of LORDS is 0.47459594 USD and is up 1.28 over the last 24 hours. It is currently trading on 12 active market(s) with $329,582.58 traded over the last 24 hours. More information can be found at https://bibliothecadao.xyz/.',
        '2024-02-19 13:22:12.003713', '2024-02-19 13:22:12.003713'),
       ('6482fd23-14b1-4d69-a957-beb647be8073', 'CRYPTO', 'Optimism', 'OP', 'https://s2.coinmarketcap.com/static/img/coins/64x64/11840.png', 18,
        'Optimism (OP) is a cryptocurrency and operates on the Optimism platform. Optimism has a current supply of 4,294,967,296 with 957,378,568 in circulation. The last known price of Optimism is 3.91661918 USD and is up 6.22 over the last 24 hours. It is currently trading on 459 active market(s) with $219,310,653.44 traded over the last 24 hours. More information can be found at https://www.optimism.io/.',
        '2024-02-19 13:22:42.216817', '2024-02-19 13:22:42.216817'),
       ('ed5ea6b5-cf94-4dd9-abd8-1b9bca44bd31', 'CRYPTO', 'Maker', 'MKR', 'https://s2.coinmarketcap.com/static/img/coins/64x64/1518.png', 18,
        'Maker (MKR) is a cryptocurrency and operates on the Ethereum platform. Maker has a current supply of 977,631.03695089 with 923,281.38795834 in circulation. The last known price of Maker is 2,018.83919871 USD and is down -3.80 over the last 24 hours. It is currently trading on 439 active market(s) with $62,814,037.45 traded over the last 24 hours. More information can be found at https://makerdao.com/.',
        '2024-02-21 16:11:57.441430', '2024-02-21 16:11:57.441430'),
       ('079bc07b-290b-4a02-a7ae-347e42c24b28', 'FIAT', 'Euro', 'EUR', null, 2, null, '2024-02-22 13:28:41.636536', '2024-02-22 13:28:41.636536')
on conflict (code) do nothing;

insert into public.erc20 (blockchain, address, name, symbol, decimals, total_supply, tech_created_at, tech_updated_at, currency_id)
values ('ethereum', '0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48', 'USD Coin', 'USDC', 6, 24383215925825126, '2024-02-19 13:21:42.214734',
        '2024-02-19 13:21:42.214734', '7bb644bd-e2e1-437b-95f3-3dfff46a7918'),
       ('ethereum', '0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0', 'Lords', 'LORDS', 18, 500000000000000000000000000, '2024-02-19 13:22:12.003713',
        '2024-02-19 13:22:12.003713', '19083c20-13b3-4f66-b78a-f7f49f33a6c3'),
       ('optimism', '0x4200000000000000000000000000000000000042', 'Optimism', 'OP', 18, 4294967295989853760711675076, '2024-02-19 13:22:42.216817',
        '2024-02-19 13:22:42.216817', '6482fd23-14b1-4d69-a957-beb647be8073'),
       ('ethereum', '0x9f8F72aA9304c8B593d555F12eF6589cC3A579A2', 'Maker', 'MKR', 18, 977631036950888222010062, '2024-02-21 16:11:57.441430',
        '2024-02-21 16:11:57.441430', 'ed5ea6b5-cf94-4dd9-abd8-1b9bca44bd31')
on conflict (blockchain, symbol) do nothing;

insert into rewards(id, project_id, requestor_id, recipient_id, currency_id, amount, requested_at)
select pr.id, pr.project_id, pr.requestor_id, pr.recipient_id, c.id, pr.amount, pr.requested_at
from payment_requests pr
         join currencies c on c.code = UPPER(pr.currency::TEXT);

insert into reward_items(reward_id, number, repo_id, id, type, project_id, recipient_id)
select wi.payment_id, wi.number, wi.repo_id, wi.id, wi.type, wi.project_id, wi.recipient_id
from marketplace_db.public.work_items wi;

insert into accounting.reward_status_data(reward_id, sponsor_has_enough_fund, unlock_date, invoice_received_at, paid_at, networks, amount_usd_equivalent)
select r.id,
       true, -- todo
       case when pr.currency = 'op' then to_date('2024-08-23', 'YYYY-MM-DD') end,
       i.created_at,
       max(p.processed_at),
       case pr.currency
           when 'usd' then '{sepa}'::network[]
           when 'eth' then '{ethereum}'::network[]
           when 'op' then '{optimism}'::network[]
           when 'strk' then '{starknet}'::network[]
           when 'lords' then '{ethereum}'::network[]
           when 'usdc' then '{ethereum}'::network[]
           end,
       case pr.currency when 'usd' then pr.amount else cuq.price * pr.amount end
from rewards r
         join payment_requests pr on pr.id = r.id
         left join accounting.invoices i on i.id = r.invoice_id
         left join payments p on p.request_id = r.id
         left join crypto_usd_quotes cuq on cuq.currency = pr.currency
group by r.id, pr.currency, pr.amount, i.created_at, cuq.price;
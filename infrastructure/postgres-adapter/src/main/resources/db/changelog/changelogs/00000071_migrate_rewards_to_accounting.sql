-- Update rewards table
DROP VIEW accounting.reward_statuses;
DROP VIEW accounting.reward_usd_equivalent_data;

alter table rewards
    add column currency_id UUID not null references currencies (id);

alter table accounting.reward_status_data
    drop column networks;

alter table accounting.reward_status_data
    add column networks accounting.network[] not null default '{}';

-- Re-create views
CREATE TYPE reward_status_as_project_lead AS ENUM ('PENDING_SIGNUP', 'PENDING_CONTRIBUTOR', 'PROCESSING', 'COMPLETE');

CREATE VIEW accounting.reward_statuses AS
WITH billing_profile_payout_networks AS
         (SELECT coalesce(w.billing_profile_id, ba.billing_profile_id)                                      AS billing_profile_id,
                 CASE WHEN w.billing_profile_id IS NOT NULL THEN array_agg(w.network) END ||
                 CASE WHEN ba.billing_profile_id IS NOT NULL THEN '{sepa, swift}'::accounting.network[] END AS networks
          FROM accounting.wallets w
                   FULL OUTER JOIN accounting.bank_accounts ba ON ba.billing_profile_id = w.billing_profile_id
          GROUP BY w.billing_profile_id, ba.billing_profile_id),

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
                 u.id IS NOT NULL                        as is_registered,
                 bp.type = 'INDIVIDUAL'                  as is_individual,
                 bp.verification_status = 'VERIFIED'     as kycb_verified,
                 coalesce(kyc.us_citizen, kyb.us_entity) as us_recipient,
                 c.code                                  as reward_currency,
                 coalesce(yutpr.yearly_usd_total, 0)     as current_year_usd_total,
                 rs.amount_usd_equivalent                as reward_usd_equivalent,
                 bppn.networks @> rs.networks            as payout_info_filled,
                 rs.sponsor_has_enough_fund,
                 rs.unlock_date,
                 rs.invoice_received_at                  as payment_requested_at,
                 rs.paid_at
          FROM accounting.reward_status_data rs
                   JOIN rewards r on r.id = rs.reward_id
                   JOIN currencies c on c.id = r.currency_id
                   LEFT JOIN iam.users u on u.github_user_id = r.recipient_id
                   LEFT JOIN accounting.payout_preferences pp on pp.project_id = r.project_id and pp.user_id = u.id
                   LEFT JOIN accounting.billing_profiles bp ON bp.id = pp.billing_profile_id
                   LEFT JOIN accounting.kyc kyc on kyc.billing_profile_id = bp.id
                   LEFT JOIN accounting.kyb kyb on kyb.billing_profile_id = bp.id
                   LEFT JOIN billing_profile_payout_networks bppn on bppn.billing_profile_id = bp.id
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
           as status_for_user,
       CASE
           WHEN s.paid_at IS NOT NULL THEN 'COMPLETE'::reward_status_as_project_lead
           WHEN NOT s.is_registered THEN 'PENDING_SIGNUP'::reward_status_as_project_lead
           WHEN s.payment_requested_at IS NULL THEN 'PENDING_CONTRIBUTOR'::reward_status_as_project_lead
           ELSE 'PROCESSING'::reward_status_as_project_lead
           END
           as status_for_project_lead
FROM aggregated_reward_status_data s
;

CREATE VIEW accounting.reward_usd_equivalent_data AS
WITH currency_quote_available_at AS
         (SELECT hq.base_id, min(hq.timestamp) as available_at
          FROM accounting.historical_quotes hq
                   JOIN currencies usd ON usd.code = 'USD' AND usd.id = hq.target_id
          GROUP BY hq.base_id)

SELECT r.id               as reward_id,
       r.requested_at     as reward_created_at,
       r.currency_id      as reward_currency_id,
       bp.tech_updated_at as kycb_verified_at,
       cqa.available_at   as currency_quote_available_at,
       rs.unlock_date     as unlock_date,
       r.amount           as reward_amount
FROM accounting.reward_status_data rs
         JOIN rewards r on r.id = rs.reward_id
         LEFT JOIN iam.users u on u.github_user_id = r.recipient_id
         LEFT JOIN accounting.payout_preferences pp on pp.project_id = r.project_id and pp.user_id = u.id
         LEFT JOIN accounting.billing_profiles bp ON bp.id = pp.billing_profile_id and bp.verification_status = 'VERIFIED'
         LEFT JOIN currency_quote_available_at cqa on cqa.base_id = r.currency_id;

-- Finalize rewards table migration
alter table rewards
    drop column currency;

-- Migrate rewards data
insert into rewards(id, project_id, requestor_id, recipient_id, currency_id, amount, requested_at)
select pr.id, pr.project_id, pr.requestor_id, pr.recipient_id, c.id, pr.amount, pr.requested_at
from payment_requests pr
         join currencies c on c.code = UPPER(pr.currency::TEXT);

insert into reward_items(reward_id, number, repo_id, id, type, project_id, recipient_id)
select wi.payment_id, wi.number, wi.repo_id, wi.id, wi.type, wi.project_id, wi.recipient_id
from work_items wi;

insert into accounting.reward_status_data(reward_id, sponsor_has_enough_fund, unlock_date, invoice_received_at, paid_at, networks, amount_usd_equivalent)
select r.id,
       true,
       case when pr.currency = 'op' then to_date('2024-08-23', 'YYYY-MM-DD') end,
       i.created_at,
       max(p.processed_at),
       case pr.currency
           when 'usd' then '{sepa}'::accounting.network[]
           when 'eth' then '{ethereum}'::accounting.network[]
           when 'op' then '{optimism}'::accounting.network[]
           when 'strk' then '{starknet}'::accounting.network[]
           when 'lords' then '{ethereum}'::accounting.network[]
           when 'usdc' then '{ethereum}'::accounting.network[]
           when 'apt' then '{aptos}'::accounting.network[]
           end,
       case pr.currency when 'usd' then pr.amount else cuq.price * pr.amount end
from rewards r
         join payment_requests pr on pr.id = r.id
         left join accounting.invoices i on i.id = r.invoice_id
         left join payments p on p.request_id = r.id
         left join crypto_usd_quotes cuq on cuq.currency = pr.currency
group by r.id, pr.currency, pr.amount, i.created_at, cuq.price;

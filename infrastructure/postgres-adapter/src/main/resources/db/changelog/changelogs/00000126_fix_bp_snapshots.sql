with bp as (select billing_profile_id
            from accounting.bank_accounts
            union
            select billing_profile_id
            from accounting.wallets)
insert
into accounting.payout_infos (billing_profile_id, tech_created_at, tech_updated_at)
select bp.billing_profile_id                                  as billing_profile_id,
       least(min(ba.tech_created_at), min(w.tech_created_at)) as tech_created_at,
       least(min(ba.tech_updated_at), min(w.tech_updated_at)) as tech_updated_at
from bp
         left join accounting.bank_accounts ba on ba.billing_profile_id = bp.billing_profile_id
         left join accounting.wallets w on w.billing_profile_id = bp.billing_profile_id
group by bp.billing_profile_id
on conflict (billing_profile_id) do nothing;

update accounting.invoices
set data = jsonb_set(data, '{billingProfileSnapshot, kycSnapshot, usCitizen}', kyc.us_citizen::text::jsonb, true)
from accounting.kyc
where kyc.billing_profile_id = invoices.billing_profile_id
  and data #> '{billingProfileSnapshot, kycSnapshot}' IS NOT NULL;

update accounting.invoices
set data = jsonb_set(data, '{billingProfileSnapshot, kybSnapshot, usEntity}', kyb.us_entity::text::jsonb, true)
from accounting.kyb
where kyb.billing_profile_id = invoices.billing_profile_id
  and data #> '{billingProfileSnapshot, kybSnapshot}' IS NOT NULL;

update accounting.invoices
set data = jsonb_set(data, '{billingProfileSnapshot, id, uuid}', to_jsonb(billing_profile_id), true);

alter table payment_requests
    drop constraint if exists payment_requests_invoice_id_fkey;

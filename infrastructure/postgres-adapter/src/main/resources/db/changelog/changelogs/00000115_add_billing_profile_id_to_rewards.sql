alter table public.rewards
    add column billing_profile_id uuid;

with invoices as (select i.id, i.billing_profile_id
                  from accounting.invoices i)
update public.rewards
set billing_profile_id = invoices.billing_profile_id
from invoices
where invoices.id = invoice_id;
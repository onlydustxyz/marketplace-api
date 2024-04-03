update accounting.reward_status_data rsd
set invoice_received_at = i.created_at
from rewards r
         join accounting.invoices i on i.id = r.invoice_id
where rsd.reward_id = r.id
  and r.invoice_id is not null
  and i.status != 'DRAFT'
  and rsd.invoice_received_at is null;

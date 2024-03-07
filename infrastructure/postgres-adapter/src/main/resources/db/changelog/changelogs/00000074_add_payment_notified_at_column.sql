alter table payment_requests
    add column payment_notified_at timestamp;

with rewards_paid as (select pr.id, p.processed_at
                      from payment_requests pr
                               join payments p on p.request_id = pr.id)
update payment_requests pr2
set payment_notified_at = rewards_paid.processed_at
from rewards_paid
where pr2.id = rewards_paid.id;
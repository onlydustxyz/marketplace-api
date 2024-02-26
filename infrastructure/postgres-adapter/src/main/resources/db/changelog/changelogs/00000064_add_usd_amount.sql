alter table payment_requests
    add column usd_amount numeric;

update payment_requests pr
set usd_amount = amount *
                 (select hq.price
                  from accounting.historical_quotes hq
                           join currencies usd on usd.id = hq.target_id and usd.code = 'USD'
                  where hq.base_id = c.id
                    and hq.timestamp > pr.requested_at
                  order by timestamp
                  limit 1)
from currencies c
where c.code = upper(pr.currency::text);

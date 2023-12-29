-- Move all payment currencies to USDC if done in crypto
UPDATE payments
SET currency_code = 'USDC'
WHERE currency_code = 'USD'
  AND receipt ?? 'Ethereum';

-- Move all payment request currencies to USDC if
--      payment is USDC, or
--      status is pending and preferred method of recipient is not FIAT
UPDATE payment_requests
SET currency='usdc'::currency
WHERE id IN (SELECT distinct request_id
             from payments
             where currency_code = 'USDC');


UPDATE payment_requests
SET currency='usdc'::currency
WHERE currency = 'usd'::currency
  AND NOT EXISTS(SELECT 1 from payments where request_id = payment_requests.id)
  AND NOT EXISTS(SELECT 1
                 from user_payout_info upi
                          join iam.users u on u.id = upi.user_id
                 where u.github_user_id = payment_requests.recipient_id
                   and upi.usd_preferred_method = 'fiat');


DELETE
FROM events
WHERE EXISTS(select 1
             from events e
             where e.aggregate_id = events.aggregate_id
               and e.payload ?? 'Cancelled'
               and e.aggregate_name = 'PAYMENT');

DELETE
FROM events
WHERE payload ?? 'Spent'
  and aggregate_name = 'BUDGET';


-- Update events
UPDATE events
SET payload = jsonb_set(payload, '{Processed, amount, currency}', '"USDC"')
FROM payments
where events.aggregate_id::uuid = payments.request_id
  AND payload ?? 'Processed'
  AND currency_code = 'USDC';

UPDATE events
SET payload = jsonb_set(payload, '{Requested, amount, currency}', '"USDC"')
FROM payment_requests
where events.aggregate_id::uuid = payment_requests.id
  AND payload ?? 'Requested'
  AND currency = 'usdc'::currency;

UPDATE events
SET payload = jsonb_set(payload, '{Created, currency}', '"USDC"')
WHERE aggregate_name = 'BUDGET'
  and payload #>> '{Created, currency}' = 'USD';

UPDATE events
SET payload = jsonb_set(payload, '{BudgetLinked, currency}', '"USDC"')
WHERE aggregate_name = 'PROJECT'
  and payload #>> '{BudgetLinked, currency}' = 'USD';


INSERT INTO events(timestamp, aggregate_name, aggregate_id, payload)
SELECT e.timestamp + interval '1 second',
       'PROJECT',
       p.project_id,
       jsonb_build_object('BudgetLinked',
                          jsonb_build_object('id', p.project_id, 'currency', 'USD', 'budget_id', gen_random_uuid()))
FROM (SELECT DISTINCT(project_id) as project_id FROM payment_requests WHERE currency = 'usd') p
         JOIN events e
              ON e.aggregate_id::uuid = p.project_id AND e.aggregate_name = 'PROJECT' AND e.payload ?? 'Created';



INSERT INTO events(timestamp, aggregate_name, aggregate_id, payload)
SELECT e.timestamp,
       'BUDGET',
       e.payload #>> '{BudgetLinked, budget_id}',
       jsonb_build_object('Created',
                          jsonb_build_object('id', e.payload #> '{BudgetLinked, budget_id}', 'currency', 'USD'))
FROM events e
WHERE e.aggregate_name = 'PROJECT'
  AND e.payload #>> '{BudgetLinked, currency}' = 'USD';



WITH spent AS (SELECT pr.project_id,
                      SUM(pr.amount) as usd_amount
               FROM payment_requests pr
               WHERE pr.currency = 'usd'
               GROUP BY pr.project_id)

INSERT
INTO events(timestamp, aggregate_name, aggregate_id, payload)
SELECT e.timestamp + interval '1 second',
       'BUDGET',
       e.payload #>> '{BudgetLinked, budget_id}',
       jsonb_build_object('Allocated', jsonb_build_object('id', e.payload #> '{BudgetLinked, budget_id}', 'amount',
                                                          spent.usd_amount::text))
FROM events e
         JOIN spent ON spent.project_id = e.aggregate_id::uuid
WHERE e.aggregate_name = 'PROJECT'
  AND e.payload #>> '{BudgetLinked, currency}' = 'USD';



WITH spent AS (SELECT pr.project_id,
                      SUM(pr.amount) as usd_amount
               FROM payment_requests pr
               WHERE pr.currency = 'usd'
               GROUP BY pr.project_id)

INSERT
INTO events(timestamp, aggregate_name, aggregate_id, payload)
SELECT now(),
       'BUDGET',
       e.payload #>> '{BudgetLinked, budget_id}',
       jsonb_build_object('Allocated', jsonb_build_object('id', e.payload #> '{BudgetLinked, budget_id}', 'amount',
                                                          (-spent.usd_amount)::text))
FROM events e
         JOIN spent ON spent.project_id = e.aggregate_id::uuid
WHERE e.aggregate_name = 'PROJECT'
  AND e.payload #>> '{BudgetLinked, currency}' = 'USDC';



INSERT INTO events(timestamp, aggregate_name, aggregate_id, payload)
SELECT epr.timestamp,
       'BUDGET',
       e.payload #>> '{BudgetLinked, budget_id}',
       jsonb_build_object('Spent', jsonb_build_object('id', e.payload #> '{BudgetLinked, budget_id}', 'amount',
                                                      epr.payload #> '{Requested, amount, amount}'))
FROM events epr
         JOIN events e ON e.aggregate_name = 'PROJECT'
    AND e.payload #> '{BudgetLinked, currency}' = epr.payload #> '{Requested, amount, currency}'
    AND e.aggregate_id = epr.payload #>> '{Requested, project_id}'
WHERE epr.aggregate_name = 'PAYMENT'
  and epr.payload ?? 'Requested';

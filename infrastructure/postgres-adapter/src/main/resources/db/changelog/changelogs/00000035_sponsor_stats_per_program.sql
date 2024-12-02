create or replace view bi.sponsor_stats_per_currency_per_program as
SELECT abt.sponsor_id,
       abt.currency_id,
       abt.program_id,
       COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'TRANSFER' AND abt.project_id IS NULL), 0) -
       COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'REFUND' AND abt.project_id IS NULL), 0)     AS total_allocated,
       COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'TRANSFER' AND abt.project_id IS NOT NULL), 0) -
       COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'REFUND' AND abt.project_id IS NOT NULL), 0) AS total_granted,
       COALESCE(count(DISTINCT abt.project_id) FILTER (WHERE abt.type = 'TRANSFER'), 0) -
       COALESCE(count(DISTINCT abt.project_id) FILTER (WHERE abt.type = 'REFUND'), 0)                 AS project_count
FROM accounting.all_transactions abt
WHERE abt.sponsor_id IS NOT NULL
  AND abt.program_id IS NOT NULL
  AND abt.reward_id IS NULL
GROUP BY abt.sponsor_id, abt.currency_id, abt.program_id;
create or replace view programs_projects as
WITH allocations AS (SELECT abt.currency_id,
                            abt.program_id,
                            abt.project_id,
                            COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'TRANSFER' AND reward_id IS NULL), 0)
                                - COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'REFUND' AND reward_id IS NULL), 0)
                                - COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'TRANSFER' AND reward_id IS NOT NULL), 0)
                                + COALESCE(sum(abt.amount) FILTER (WHERE abt.type = 'REFUND' AND reward_id IS NOT NULL), 0) AS remaining_amount
                     FROM accounting.account_book_transactions abt
                     WHERE abt.project_id IS NOT NULL
                       AND abt.payment_id IS NULL
                     GROUP BY abt.currency_id, abt.program_id, abt.project_id)
select distinct program_id, project_id
from allocations
where remaining_amount > 0;

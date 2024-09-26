ALTER TABLE iam.notification_channels
    DROP CONSTRAINT IF EXISTS notification_channels_pk;
ALTER TABLE iam.notification_channels
    ADD CONSTRAINT notification_channels_pk PRIMARY KEY (notification_id, channel);

create index if not exists notification_channels_read_at_index
    on iam.notification_channels (read_at);

create index if not exists notifications_recipient_id_index
    on iam.notifications (recipient_id);


DROP VIEW programs_projects;
CREATE MATERIALIZED VIEW m_programs_projects AS
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
SELECT program_id, project_id, bool_or(remaining_amount > 0) as has_remaining_grants
FROM allocations
GROUP BY program_id, project_id;

CREATE UNIQUE INDEX m_programs_projects_pk
    ON m_programs_projects (program_id, project_id);

CREATE UNIQUE INDEX m_programs_projects_pk_inv
    ON m_programs_projects (project_id, program_id);

CREATE VIEW active_programs_projects AS
SELECT program_id, project_id
FROM m_programs_projects
WHERE has_remaining_grants IS TRUE;

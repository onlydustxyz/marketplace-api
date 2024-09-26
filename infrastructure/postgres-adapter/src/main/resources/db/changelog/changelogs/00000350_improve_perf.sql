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



create or replace view bi.program_stats_per_currency_per_project as
select abt.program_id                                                                                      as program_id,
       abt.currency_id                                                                                     as currency_id,
       abt.project_id                                                                                      as project_id,
       coalesce(sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.reward_id is null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type = 'REFUND' and abt.reward_id is null), 0)     as total_granted,
       coalesce(sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.reward_id is not null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type = 'REFUND' and abt.reward_id is not null), 0) as total_rewarded,
       coalesce(count(distinct abt.reward_id) filter (where abt.type = 'TRANSFER'), 0) -
       coalesce(count(distinct abt.reward_id) filter (where abt.type = 'REFUND'), 0)                       as reward_count
from accounting.account_book_transactions abt
where abt.program_id is not null
  and abt.project_id is not null
  and abt.payment_id is null
group by abt.program_id,
         abt.currency_id,
         abt.project_id;


create or replace view bi.program_stats as
with project_users as (select pc.project_id, pc.github_user_id as contributor_id
                       from projects_contributors pc
                       union
                       select pl.project_id, u.github_user_id as contributor_id
                       from project_leads pl
                                join iam.users u on u.id = pl.user_id)
select p.id                                           as program_id,
       coalesce(count(distinct s.project_id), 0)      as granted_project_count,
       coalesce(sum(s.reward_count)::bigint, 0)       as reward_count,
       coalesce(count(distinct pu.contributor_id), 0) as user_count
from programs p
         left join bi.program_stats_per_currency_per_project s on p.id = s.program_id and s.total_granted > 0
         left join project_users pu on pu.project_id = s.project_id
group by p.id;

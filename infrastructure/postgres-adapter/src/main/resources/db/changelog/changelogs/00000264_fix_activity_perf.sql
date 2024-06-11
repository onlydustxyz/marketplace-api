-- Improve join on github_pull_requests by casting the right side of the join
CREATE OR REPLACE VIEW public_received_rewards AS
select r.*,
       rsd.amount_usd_equivalent,
       rsd.usd_conversion_rate,
       rsd.invoice_received_at,
       rsd.paid_at,
       rsd.networks,
       coalesce(array_agg(distinct unnested.main_file_extensions)
                filter (where unnested.main_file_extensions is not null), '{}') as main_file_extensions
from rewards r
         join reward_items ri on ri.reward_id = r.id
         join projects p on p.id = r.project_id and p.visibility = 'PUBLIC'
         join accounting.reward_status_data rsd ON rsd.reward_id = r.id
         left join indexer_exp.github_pull_requests gpr
                   on gpr.id = (case when ri.type = 'PULL_REQUEST' then cast(ri.id as bigint) else null end)
         left join unnest(gpr.main_file_extensions) unnested(main_file_extensions) on true
group by r.id, rsd.reward_id;


-- add missing index
create index contributions_github_author_id_index
    on indexer_exp.contributions (github_author_id);


-- make public_activity a materialized view
drop view public_activity;

create materialized view public_activity as
(select distinct on (c.repo_id) cast('PULL_REQUEST' as activity_type) as type,
                                c.completed_at                        as timestamp,
                                c.github_author_id                    as pull_request_author_id,
                                c.project_ids[1]                      as project_id,
                                cast(NULL as uuid)                    as reward_id
 from (select distinct on (c.github_author_id) c.*
       from (select distinct on (c.pull_request_id) c.*
             from public_contributions c
                      join iam.users u on u.github_user_id = c.github_author_id
             where c.type = 'PULL_REQUEST'
               and c.status = 'COMPLETED'
               and array_length(c.main_file_extensions, 1) > 0) c
       order by c.github_author_id, c.completed_at desc) c
 order by c.repo_id, c.completed_at desc)

UNION

(select distinct on (r.requestor_id) cast('REWARD_CREATED' as activity_type) as type,
                                     r.requested_at                          as timestamp,
                                     cast(NULL as bigint)                    as pull_request_author_id,
                                     r.project_id                            as project_id,
                                     r.id                                    as reward_id
 from public_received_rewards r
 order by r.requestor_id, r.requested_at desc)

UNION

(select distinct on (r.invoice_id) cast('REWARD_CLAIMED' as activity_type) as type,
                                   r.invoice_received_at                   as timestamp,
                                   cast(NULL as bigint)                    as pull_request_author_id,
                                   r.project_id                            as project_id,
                                   r.id                                    as reward_id
 from public_received_rewards r
 where r.invoice_received_at is not null
 order by r.invoice_id, r.invoice_received_at desc)

UNION

(select cast('PROJECT_CREATED' as activity_type) as type,
        p.created_at                             as timestamp,
        cast(NULL as bigint)                     as pull_request_author_id,
        p.id                                     as project_id,
        cast(NULL as uuid)                       as reward_id
 from projects p
 order by p.created_at desc)
;


create unique index public_activity_pk
    on public_activity (timestamp desc, type, project_id, pull_request_author_id, reward_id);

refresh materialized view public_activity;
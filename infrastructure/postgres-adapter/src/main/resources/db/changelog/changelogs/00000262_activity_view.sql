create type activity_type as enum ('PULL_REQUEST', 'REWARD_CREATED', 'REWARD_CLAIMED', 'PROJECT_CREATED');

create view public_activity as
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

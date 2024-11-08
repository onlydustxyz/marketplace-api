call drop_pseudo_projection('bi', 'application_data');
call drop_pseudo_projection('bi', 'project_contributions_data');
call drop_pseudo_projection('bi', 'per_contributor_contribution_data');
call drop_pseudo_projection('bi', 'contribution_reward_data');
call drop_pseudo_projection('bi', 'contribution_data');

call create_pseudo_projection('bi', 'contribution_data', $$
with ranked_project_github_repos_relationship AS (SELECT *,
                                                         row_number() OVER (PARTITION BY github_repo_id ORDER BY project_id) as row_number
                                                  FROM project_github_repos)
select c.contribution_uuid                                                                                      as contribution_uuid,
       c.repo_id                                                                                                as repo_id,
       p.id                                                                                                     as project_id,
       p.slug                                                                                                   as project_slug,
       c.created_at                                                                                             as timestamp,
       c.status                                                                                                 as contribution_status,
       c.type                                                                                                   as contribution_type,
       coalesce(c.pull_request_id::text, c.issue_id::text, c.code_review_id)                                    as github_id,
       c.github_author_id                                                                                       as github_author_id,
       c.github_number                                                                                          as github_number,
       c.github_status                                                                                          as github_status,
       c.github_title                                                                                           as github_title,
       c.github_html_url                                                                                        as github_html_url,
       c.github_body                                                                                            as github_body,
       c.created_at                                                                                             as created_at,
       c.updated_at                                                                                             as updated_at,
       c.completed_at                                                                                           as completed_at,
       c.issue_id                                                                                               as issue_id,
       c.pull_request_id                                                                                        as pull_request_id,
       c.code_review_id                                                                                         as code_review_id,
       (c.type = 'ISSUE')::int                                                                                  as is_issue,
       (c.type = 'PULL_REQUEST')::int                                                                           as is_pr,
       (c.type = 'CODE_REVIEW')::int                                                                            as is_code_review,
       case
           when agc.contribution_uuid is not null then 'ARCHIVED'::activity_status
           when c.type = 'ISSUE' then
               case
                   when c.github_status = 'OPEN' AND bool_and(gia.user_id is null) then 'NOT_ASSIGNED'::activity_status
                   when c.github_status = 'OPEN' AND bool_or(gia.user_id is not null) then 'IN_PROGRESS'::activity_status
                   else 'DONE'::activity_status
                   end
           when c.type = 'PULL_REQUEST' then
               case
                   when c.github_status = 'DRAFT' then 'IN_PROGRESS'::activity_status
                   when c.github_status = 'OPEN' then 'TO_REVIEW'::activity_status
                   else 'DONE'::activity_status
                   end
           when c.type = 'CODE_REVIEW' then
               case
                   when c.pr_review_state in ('PENDING_REVIEWER', 'UNDER_REVIEW') then 'IN_PROGRESS'::activity_status
                   else 'DONE'::activity_status
                   end
           end                                                                                                  as activity_status,
       c.github_comments_count                                                                                  as github_comment_count,
       array_agg(distinct l.id) filter ( where l.id is not null )                                               as language_ids,
       array_agg(distinct pe.ecosystem_id) filter ( where pe.ecosystem_id is not null )                         as ecosystem_ids,
       array_agg(distinct pp.program_id) filter ( where pp.program_id is not null )                             as program_ids,
       array_agg(distinct ppc.project_category_id)
       filter ( where ppc.project_category_id is not null )                                                     as project_category_ids,
       bool_or(gl.name ~~* '%good%first%issue%')                                                                as is_good_first_issue,
       array_agg(distinct gil.label_id)
       filter ( where gil.label_id is not null )                                                                as github_label_ids,
       array_agg(distinct ci.issue_id) filter ( where ci.issue_id is not null )                                 as closing_issue_ids,
       jsonb_build_object(
               'id', gr.id,
               'owner', gr.owner_login,
               'name', gr.name,
               'description', gr.description,
               'htmlUrl', gr.html_url)                                                                          as github_repo,

       case
           when p.id is not null then jsonb_build_object(
                   'id', p.id,
                   'slug', p.slug,
                   'name', p.name,
                   'logoUrl', p.logo_url) end                                                                   as project,

       jsonb_agg(distinct jsonb_build_object('name', gl.name,
                                             'description', gl.description)) filter ( where gl.id is not null ) as github_labels,

       jsonb_agg(distinct jsonb_build_object('id', l.id,
                                             'slug', l.slug,
                                             'name', l.name,
                                             'logoUrl', l.logo_url,
                                             'bannerUrl', l.banner_url)) filter ( where l.id is not null )      as languages,

       jsonb_agg(distinct jsonb_build_object('type', 'ISSUE',
                                             'contributionUuid', i.contribution_uuid,
                                             'githubId', i.id,
                                             'githubNumber', i.number,
                                             'githubStatus', i.status,
                                             'githubTitle', i.title,
                                             'githubHtmlUrl', i.html_url)) filter ( where i.id is not null )    as linked_issues,
       concat(c.github_number, ' ',
              c.github_title, ' ',
              gr.owner_login, ' ',
              gr.name, ' ',
              string_agg(gl.name, ' '), ' ',
              string_agg(l.name, ' '), ' ',
              string_agg(i.number || ' ' || i.title, ' ')
       )                                                                                                        as search
from indexer_exp.grouped_contributions c
         left join indexer_exp.github_repos gr on gr.id = c.repo_id
         left join ranked_project_github_repos_relationship pgr on pgr.github_repo_id = c.repo_id and pgr.row_number = 1
         left join projects p on p.id = pgr.project_id
         left join language_file_extensions lfe on lfe.extension = any (c.main_file_extensions)
         left join languages l on l.id = lfe.language_id
         left join projects_ecosystems pe on pe.project_id = p.id
         left join m_programs_projects pp on pp.project_id = p.id
         left join projects_project_categories ppc on ppc.project_id = p.id
         left join indexer_exp.github_issues_labels gil ON gil.issue_id = c.issue_id
         left join indexer_exp.github_labels gl ON gil.label_id = gl.id
         left join indexer_exp.github_issues_assignees gia ON gia.issue_id = c.issue_id
         left join indexer_exp.github_code_reviews cr on cr.id = c.code_review_id
         left join indexer_exp.github_pull_requests_closing_issues ci on ci.pull_request_id = c.pull_request_id
         left join indexer_exp.github_issues i on i.id = ci.issue_id
         left join archived_github_contributions agc on agc.contribution_uuid = c.contribution_uuid
group by c.contribution_uuid,
         c.repo_id,
         p.id,
         p.slug,
         c.created_at,
         c.type,
         c.status,
         c.pull_request_id,
         c.issue_id,
         c.github_number,
         c.github_status,
         c.github_title,
         c.github_html_url,
         c.github_body,
         c.pr_review_state,
         c.created_at,
         c.updated_at,
         c.completed_at,
         cr.pull_request_id,
         gr.id,
         agc.contribution_uuid
$$, 'contribution_uuid');

create index on bi.p_contribution_data (created_at);
create index on bi.p_contribution_data (contribution_type);
create index on bi.p_contribution_data (activity_status);
create index on bi.p_contribution_data (project_id);
create index on bi.p_contribution_data (project_slug);
create index on bi.p_contribution_data (repo_id);
create index on bi.p_contribution_data (project_id, timestamp);
create index on bi.p_contribution_data (timestamp, project_id);

-- typical filters used by the frontend
create index on bi.p_contribution_data (activity_status, project_id, created_at);
create index on bi.p_contribution_data (activity_status, project_id, contribution_type);
create index on bi.p_contribution_data (activity_status, contribution_type, created_at);
create index on bi.p_contribution_data (project_id, contribution_status, timestamp);
create unique index on bi.p_contribution_data (project_id, contribution_type, contribution_uuid);



call create_pseudo_projection('bi', 'contribution_reward_data', $$
select per_recipient.contribution_uuid                                                                     as contribution_uuid,
       per_recipient.repo_id                                                                               as repo_id,
       array_uniq_cat_agg(per_recipient.reward_ids)                                                        as reward_ids,
       sum(per_recipient.total_rewarded_usd_amount)                                                        as total_rewarded_usd_amount,
       jsonb_agg(jsonb_build_object('recipientId', per_recipient.recipient_id,
                                    'rewardIds', per_recipient.reward_ids,
                                    'totalRewardedUsdAmount', per_recipient.total_rewarded_usd_amount))    as per_recipient
from (select c.contribution_uuid                     as contribution_uuid,
             c.repo_id                               as repo_id,
             r.recipient_id                          as recipient_id,
             array_agg(ri.reward_id)                 as reward_ids,
             sum(round(rd.amount_usd_equivalent, 2)) as total_rewarded_usd_amount
      from indexer_exp.grouped_contributions c
               join reward_items ri on ri.contribution_uuid = c.contribution_uuid
               join rewards r on r.id = ri.reward_id
               join accounting.reward_status_data rd on rd.reward_id = ri.reward_id
      group by c.contribution_uuid, c.repo_id, r.recipient_id) as per_recipient
group by per_recipient.contribution_uuid, per_recipient.repo_id
$$, 'contribution_uuid');

create unique index on bi.p_contribution_reward_data (contribution_uuid, total_rewarded_usd_amount);
create unique index on bi.p_contribution_reward_data (repo_id, contribution_uuid);
create index on bi.p_contribution_reward_data using gin (reward_ids);



call create_pseudo_projection('bi', 'per_contributor_contribution_data', $$
select md5(row (c.contribution_uuid, cd.contributor_id)::text)::uuid      as technical_id,
       c.contribution_uuid                                                as contribution_uuid,
       c.repo_id                                                          as repo_id,
       c.project_id                                                       as project_id,
       c.project_slug                                                     as project_slug,
       cd.contributor_id                                                  as contributor_id,
       u.id                                                               as contributor_user_id,
       (array_agg(kyc.country) filter (where kyc.country is not null))[1] as contributor_country,
       c.created_at                                                       as timestamp,
       c.contribution_status                                              as contribution_status,
       c.contribution_type                                                as contribution_type,
       c.github_author_id                                                 as github_author_id,
       c.github_number                                                    as github_number,
       c.github_status                                                    as github_status,
       c.github_title                                                     as github_title,
       c.github_html_url                                                  as github_html_url,
       c.github_body                                                      as github_body,
       c.created_at                                                       as created_at,
       c.updated_at                                                       as updated_at,
       c.completed_at                                                     as completed_at,
       date_trunc('day', c.created_at)                                    as day_timestamp,
       date_trunc('week', c.created_at)                                   as week_timestamp,
       date_trunc('month', c.created_at)                                  as month_timestamp,
       date_trunc('quarter', c.created_at)                                as quarter_timestamp,
       date_trunc('year', c.created_at)                                   as year_timestamp,
       not exists(select 1
                  from indexer_exp.contributions fc
                           join indexer_exp.github_repos gr on gr.id = fc.repo_id
                           join project_github_repos pgr on pgr.github_repo_id = gr.id
                  where fc.contributor_id = cd.contributor_id
                    and fc.created_at < c.created_at)                     as is_first_contribution_on_onlydust,
       c.is_issue                                                         as is_issue,
       c.is_pr                                                            as is_pr,
       c.is_code_review                                                   as is_code_review,
       c.activity_status                                                  as activity_status,
       c.language_ids                                                     as language_ids,
       c.ecosystem_ids                                                    as ecosystem_ids,
       c.program_ids                                                      as program_ids,
       c.project_category_ids                                             as project_category_ids,
       c.languages                                                        as languages,
       c.is_good_first_issue                                              as is_good_first_issue,
       ccd.assignee_ids                                                   as assignee_ids,
       c.github_label_ids                                                 as github_label_ids,
       c.closing_issue_ids                                                as closing_issue_ids,
       ccd.applicant_ids                                                  as applicant_ids
from bi.p_contribution_data c
         join bi.p_contribution_contributors_data ccd on c.contribution_uuid = ccd.contribution_uuid
         cross join unnest(ccd.contributor_ids) as cd(contributor_id)
         left join iam.users u on u.github_user_id = cd.contributor_id
         left join accounting.billing_profiles_users bpu on bpu.user_id = u.id
         left join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id
group by c.contribution_uuid,
         ccd.contribution_uuid,
         cd.contributor_id,
         u.id
$$, 'technical_id');

create index on bi.p_per_contributor_contribution_data (contribution_uuid);
create index on bi.p_per_contributor_contribution_data (project_id);
create index on bi.p_per_contributor_contribution_data (project_slug);
create unique index on bi.p_per_contributor_contribution_data (contributor_id, contribution_uuid);
create unique index on bi.p_per_contributor_contribution_data (contributor_user_id, contribution_uuid);
create index on bi.p_per_contributor_contribution_data (contributor_id, project_id, timestamp desc);

create index bi_contribution_data_repo_id_idx on bi.p_per_contributor_contribution_data (repo_id);

create index bi_contribution_data_project_id_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, timestamp);
create index bi_contribution_data_project_id_day_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, day_timestamp);
create index bi_contribution_data_project_id_week_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, week_timestamp);
create index bi_contribution_data_project_id_month_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, month_timestamp);
create index bi_contribution_data_project_id_quarter_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, quarter_timestamp);
create index bi_contribution_data_project_id_year_timestamp_idx on bi.p_per_contributor_contribution_data (project_id, year_timestamp);
create index bi_contribution_data_project_id_timestamp_idx_inv on bi.p_per_contributor_contribution_data (timestamp, project_id);
create index bi_contribution_data_project_id_day_timestamp_idx_inv on bi.p_per_contributor_contribution_data (day_timestamp, project_id);
create index bi_contribution_data_project_id_week_timestamp_idx_inv on bi.p_per_contributor_contribution_data (week_timestamp, project_id);
create index bi_contribution_data_project_id_month_timestamp_idx_inv on bi.p_per_contributor_contribution_data (month_timestamp, project_id);
create index bi_contribution_data_project_id_quarter_timestamp_idx_inv on bi.p_per_contributor_contribution_data (quarter_timestamp, project_id);

create index bi_contribution_data_project_id_year_timestamp_idx_inv on bi.p_per_contributor_contribution_data (year_timestamp, project_id);
create index bi_contribution_data_contributor_id_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, timestamp);
create index bi_contribution_data_contributor_id_day_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, day_timestamp);
create index bi_contribution_data_contributor_id_week_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, week_timestamp);
create index bi_contribution_data_contributor_id_month_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, month_timestamp);
create index bi_contribution_data_contributor_id_quarter_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, quarter_timestamp);
create index bi_contribution_data_contributor_id_year_timestamp_idx on bi.p_per_contributor_contribution_data (contributor_id, year_timestamp);
create index bi_contribution_data_contributor_id_timestamp_idx_inv on bi.p_per_contributor_contribution_data (timestamp, contributor_id);
create index bi_contribution_data_contributor_id_day_timestamp_idx_inv on bi.p_per_contributor_contribution_data (day_timestamp, contributor_id);
create index bi_contribution_data_contributor_id_week_timestamp_idx_inv on bi.p_per_contributor_contribution_data (week_timestamp, contributor_id);
create index bi_contribution_data_contributor_id_month_timestamp_idx_inv on bi.p_per_contributor_contribution_data (month_timestamp, contributor_id);
create index bi_contribution_data_contributor_id_quarter_timestamp_idx_inv on bi.p_per_contributor_contribution_data (quarter_timestamp, contributor_id);

create index bi_contribution_data_contributor_id_year_timestamp_idx_inv on bi.p_per_contributor_contribution_data (year_timestamp, contributor_id);



call create_pseudo_projection('bi', 'project_contributions_data', $$
SELECT p.id                                                                            as project_id,
       array_agg(distinct cd.repo_id)                                                  as repo_ids,
       count(distinct cd.contributor_id)                                               as contributor_count,
       count(distinct cd.contribution_uuid) filter ( where cd.is_good_first_issue and
                                                coalesce(array_length(cd.assignee_ids, 1), 0) = 0 and
                                                cd.contribution_status != 'COMPLETED' and
                                                cd.contribution_status != 'CANCELLED') as good_first_issue_count
FROM projects p
         LEFT JOIN bi.p_per_contributor_contribution_data cd ON cd.project_id = p.id
GROUP BY p.id
$$, 'project_id');

create unique index on bi.p_project_contributions_data (project_id, contributor_count, good_first_issue_count);
create index on bi.p_project_contributions_data using gin (repo_ids);



call create_pseudo_projection('bi', 'application_data', $$
select a.id                                       as application_id,
       cd.contribution_uuid                       as contribution_uuid,
       a.received_at                              as timestamp,
       date_trunc('day', a.received_at)           as day_timestamp,
       date_trunc('week', a.received_at)          as week_timestamp,
       date_trunc('month', a.received_at)         as month_timestamp,
       date_trunc('quarter', a.received_at)       as quarter_timestamp,
       date_trunc('year', a.received_at)          as year_timestamp,
       a.applicant_id                             as contributor_id,
       a.origin                                   as origin,
       case
           when array [a.applicant_id] <@ ccd.assignee_ids then 'ACCEPTED'::application_status
           when array_length(ccd.assignee_ids, 1) > 0 then 'SHELVED'::application_status
           else 'PENDING'::application_status end as status,
       cd.project_id                              as project_id,
       cd.project_slug                            as project_slug,
       cd.repo_id                                 as repo_id,
       cd.ecosystem_ids                           as ecosystem_ids,
       cd.program_ids                             as program_ids,
       cd.language_ids                            as language_ids,
       cd.project_category_ids                    as project_category_ids,
       concat(cd.search, ' ', iu.login)           as search
from applications a
         join bi.p_contribution_data cd on cd.issue_id = a.issue_id
         join bi.p_contribution_contributors_data ccd on ccd.contribution_uuid = cd.contribution_uuid
         left join iam.all_indexed_users iu on iu.github_user_id = a.applicant_id
group by a.id, cd.contribution_uuid, ccd.contribution_uuid, iu.login
    $$, 'application_id');

create index on bi.p_application_data (contribution_uuid);
create index on bi.p_application_data (repo_id);
create unique index if not exists p_application_data_contributor_id_status_application_id_uindex
    on bi.p_application_data (contributor_id, status, application_id);

create index bi_p_application_data_project_id_timestamp_idx on bi.p_application_data (project_id, timestamp);
create index bi_p_application_data_project_id_day_timestamp_idx on bi.p_application_data (project_id, day_timestamp);
create index bi_p_application_data_project_id_week_timestamp_idx on bi.p_application_data (project_id, week_timestamp);
create index bi_p_application_data_project_id_month_timestamp_idx on bi.p_application_data (project_id, month_timestamp);
create index bi_p_application_data_project_id_quarter_timestamp_idx on bi.p_application_data (project_id, quarter_timestamp);
create index bi_p_application_data_project_id_year_timestamp_idx on bi.p_application_data (project_id, year_timestamp);
create index bi_p_application_data_project_id_timestamp_idx_inv on bi.p_application_data (timestamp, project_id);
create index bi_p_application_data_project_id_day_timestamp_idx_inv on bi.p_application_data (day_timestamp, project_id);
create index bi_p_application_data_project_id_week_timestamp_idx_inv on bi.p_application_data (week_timestamp, project_id);
create index bi_p_application_data_project_id_month_timestamp_idx_inv on bi.p_application_data (month_timestamp, project_id);
create index bi_p_application_data_project_id_quarter_timestamp_idx_inv on bi.p_application_data (quarter_timestamp, project_id);
create index bi_p_application_data_project_id_year_timestamp_idx_inv on bi.p_application_data (year_timestamp, project_id);

create index bi_p_application_data_contributor_id_timestamp_idx on bi.p_application_data (contributor_id, timestamp);
create index bi_p_application_data_contributor_id_day_timestamp_idx on bi.p_application_data (contributor_id, day_timestamp);
create index bi_p_application_data_contributor_id_week_timestamp_idx on bi.p_application_data (contributor_id, week_timestamp);
create index bi_p_application_data_contributor_id_month_timestamp_idx on bi.p_application_data (contributor_id, month_timestamp);
create index bi_p_application_data_contributor_id_quarter_timestamp_idx on bi.p_application_data (contributor_id, quarter_timestamp);
create index bi_p_application_data_contributor_id_year_timestamp_idx on bi.p_application_data (contributor_id, year_timestamp);
create index bi_p_application_data_contributor_id_timestamp_idx_inv on bi.p_application_data (timestamp, contributor_id);
create index bi_p_application_data_contributor_id_day_timestamp_idx_inv on bi.p_application_data (day_timestamp, contributor_id);
create index bi_p_application_data_contributor_id_week_timestamp_idx_inv on bi.p_application_data (week_timestamp, contributor_id);
create index bi_p_application_data_contributor_id_month_timestamp_idx_inv on bi.p_application_data (month_timestamp, contributor_id);
create index bi_p_application_data_contributor_id_quarter_timestamp_idx_inv on bi.p_application_data (quarter_timestamp, contributor_id);
create index bi_p_application_data_contributor_id_year_timestamp_idx_inv on bi.p_application_data (year_timestamp, contributor_id);
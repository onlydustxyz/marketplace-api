-- call create_pseudo_projection('bi', 'contribution_data', $$...$$);
create or replace view bi.v_contribution_data as
SELECT v.*, md5(v::text) as hash
FROM (with ranked_project_github_repos_relationship AS (SELECT *,
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
         agc.contribution_uuid) v;


call refresh_pseudo_projection('bi', 'contribution_data', 'contribution_uuid');
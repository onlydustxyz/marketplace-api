ALTER TABLE languages 
ADD COLUMN transparent_logo_url TEXT,
ADD COLUMN color TEXT; 

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
         join indexer_exp.github_repos gr on gr.id = c.repo_id and gr.visibility = 'PUBLIC'
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

-- call create_pseudo_projection('bi', 'contributor_global_data', $$...$$);
create or replace view bi.v_contributor_global_data as
SELECT v.*, md5(v::text) as hash
FROM (SELECT c.*,
             bi.search_of(c.contributor_login, c.projects, c.categories, c.languages, c.ecosystems,
                          c.programs) as search

      FROM (SELECT c.*,
                   (select kyc.country
                    from iam.users u
                             join accounting.billing_profiles_users bpu on bpu.user_id = u.id
                             join accounting.kyc
                                  on kyc.billing_profile_id = bpu.billing_profile_id and kyc.country is not null
                    where u.github_user_id = c.contributor_id
                    limit 1)                                         as contributor_country,

                   (select jsonb_build_object('githubUserId', u.github_user_id,
                                              'login', u.login,
                                              'avatarUrl', u.avatar_url,
                                              'isRegistered', u.user_id is not null,
                                              'id', u.user_id,
                                              'bio', u.bio,
                                              'signedUpAt', u.signed_up_at,
                                              'signedUpOnGithubAt', u.signed_up_on_github_at,
                                              'followerCount', coalesce(ga.follower_count, 0),
                                              'globalRank', gur.rank,
                                              'globalRankPercentile', gur.rank_percentile,
                                              'globalRankCategory', case
                                                                        when gur.rank_percentile <= 0.02 then 'A'
                                                                        when gur.rank_percentile <= 0.04 then 'B'
                                                                        when gur.rank_percentile <= 0.06 then 'C'
                                                                        when gur.rank_percentile <= 0.08 then 'D'
                                                                        when gur.rank_percentile <= 0.10 then 'E'
                                                                        else 'F'
                                                  end,
                                              'contacts', (select jsonb_agg(jsonb_build_object('channel', ci.channel,
                                                                                               'contact', ci.contact,
                                                                                               'visibility',
                                                                                               case when ci.public then 'public' else 'private' end))
                                                           from contact_informations ci
                                                           where ci.user_id = u.user_id
                                                             and ci.contact != '')) as json

                    from iam.all_users u
                             left join global_users_ranks gur on gur.github_user_id = u.github_user_id
                             left join indexer_exp.github_accounts ga on ga.id = u.github_user_id
                    where u.github_user_id = c.contributor_id)       as contributor,

                   (select jsonb_agg(jsonb_build_object('id', p.id,
                                                        'slug', p.slug,
                                                        'name', p.name,
                                                        'logoUrl', p.logo_url))
                    from projects p
                    where p.id = any (c.maintained_project_ids))     as maintained_projects,

                   (select jsonb_agg(jsonb_build_object('id', p.id,
                                                        'slug', p.slug,
                                                        'name', p.name,
                                                        'logoUrl', p.logo_url))
                    from projects p
                    where p.id = any (c.contributed_on_project_ids)) as projects,

                   (select jsonb_agg(jsonb_build_object('id', l.id,
                                                        'slug', l.slug,
                                                        'name', l.name,
                                                        'logoUrl', l.logo_url,
                                                        'bannerUrl', l.banner_url))
                    from languages l
                    where l.id = any (c.language_ids))               as languages,

                   (select jsonb_agg(jsonb_build_object('id', e.id,
                                                        'slug', e.slug,
                                                        'name', e.name,
                                                        'logoUrl', e.logo_url,
                                                        'bannerUrl', e.banner_url,
                                                        'url', e.url))
                    from ecosystems e
                    where e.id = any (c.ecosystem_ids))              as ecosystems,

                   (select jsonb_agg(jsonb_build_object('id', pc.id,
                                                        'slug', pc.slug,
                                                        'name', pc.name,
                                                        'description', pc.description,
                                                        'iconSlug', pc.icon_slug))
                    from project_categories pc
                    where pc.id = any (c.project_category_ids))      as categories,

                   (select jsonb_agg(jsonb_build_object('id', prog.id,
                                                        'name', prog.name)) as json
                    from programs prog
                    where prog.id = any (c.program_ids))             as programs

            FROM (SELECT ga.id                                                                        as contributor_id,
                         ga.login                                                                     as contributor_login,
                         u.id                                                                         as contributor_user_id,

                         min(p.name)                                                                  as first_project_name,

                         array_agg(distinct pl.project_id) filter ( where pl.project_id is not null ) as maintained_project_ids,
                         array_agg(distinct p.id) filter ( where p.id is not null )                   as contributed_on_project_ids,
                         array_agg(distinct p.slug) filter ( where p.slug is not null )               as contributed_on_project_slugs,
                         array_agg(distinct ppc.project_category_id)
                         filter ( where ppc.project_category_id is not null )                         as project_category_ids,
                         array_agg(distinct lfe.language_id)
                         filter ( where lfe.language_id is not null )                                 as language_ids,
                         array_agg(distinct pe.ecosystem_id)
                         filter ( where pe.ecosystem_id is not null )                                 as ecosystem_ids,
                         array_agg(distinct pp.program_id)
                         filter ( where pp.program_id is not null )                                   as program_ids
                  FROM indexer_exp.github_accounts ga
                           LEFT JOIN indexer_exp.repos_contributors rc ON rc.contributor_id = ga.id
                           LEFT JOIN project_github_repos pgr ON pgr.github_repo_id = rc.repo_id
                           LEFT JOIN projects p on p.id = pgr.project_id
                           LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
                           LEFT JOIN m_programs_projects pp ON pp.project_id = p.id
                           LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
                           LEFT JOIN indexer_exp.github_user_file_extensions gufe ON gufe.user_id = ga.id
                           LEFT JOIN language_file_extensions lfe ON lfe.extension = gufe.file_extension
                           LEFT JOIN iam.users u on u.github_user_id = ga.id
                           LEFT JOIN project_leads pl on pl.user_id = u.id
                  GROUP BY ga.id, u.id) c) c) v;


call refresh_pseudo_projection('bi', 'contributor_global_data', 'contributor_id');

-- call create_pseudo_projection('bi', 'per_contributor_contribution_data', $$...$$);
create or replace view bi.v_per_contributor_contribution_data as
SELECT v.*, md5(v::text) as hash
FROM (select md5(row (c.contribution_uuid, cd.contributor_id)::text)::uuid      as technical_id,
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
         u.id) v;

call refresh_pseudo_projection('bi', 'per_contributor_contribution_data', 'technical_id');

-- call create_pseudo_projection('bi', 'project_global_data', $$...$$);
create or replace view bi.v_project_global_data as
SELECT v.*, md5(v::text) as hash
FROM (SELECT p.id                                                                 as project_id,
       p.slug                                                               as project_slug,
       p.created_at                                                         as created_at,
       p.rank                                                               as rank,
       jsonb_build_object('id', p.id,
                          'slug', p.slug,
                          'name', p.name,
                          'logoUrl', p.logo_url,
                          'shortDescription', p.short_description,
                          'hiring', p.hiring,
                          'visibility', p.visibility)                       as project,
       p.name                                                               as project_name,
       p.visibility                                                         as project_visibility,
       array_agg(distinct uleads.id) filter ( where uleads.id is not null ) as project_lead_ids,
       array_agg(distinct uinvleads.id)
       filter ( where uinvleads.id is not null )                            as invited_project_lead_ids,
       array_agg(distinct pc.id) filter ( where pc.id is not null )         as project_category_ids,
       array_agg(distinct pc.slug) filter ( where pc.slug is not null )     as project_category_slugs,
       array_agg(distinct l.id) filter ( where l.id is not null )           as language_ids,
       array_agg(distinct l.slug) filter ( where l.slug is not null )       as language_slugs,
       array_agg(distinct e.id) filter ( where e.id is not null )           as ecosystem_ids,
       array_agg(distinct e.slug) filter ( where e.slug is not null )       as ecosystem_slugs,
       array_agg(distinct prog.id) filter ( where prog.id is not null )     as program_ids,
       array_agg(distinct pgr.github_repo_id)
       filter ( where pgr.github_repo_id is not null )                      as repo_ids,
       array_agg(distinct pt.tag) filter ( where pt.tag is not null )       as tags,

       jsonb_agg(distinct jsonb_build_object('id', uleads.id,
                                             'login', uleads.github_login,
                                             'githubUserId', uleads.github_user_id,
                                             'avatarUrl',
                                             user_avatar_url(uleads.github_user_id, uleads.github_avatar_url)
                          )) filter ( where uleads.id is not null )         as leads,

       jsonb_agg(distinct jsonb_build_object('id', pc.id,
                                             'slug', pc.slug,
                                             'name', pc.name,
                                             'description', pc.description,
                                             'iconSlug', pc.icon_slug))
       filter ( where pc.id is not null )                                   as categories,

       jsonb_agg(distinct jsonb_build_object('id', l.id,
                                             'slug', l.slug,
                                             'name', l.name,
                                             'logoUrl', l.logo_url,
                                             'bannerUrl', l.banner_url,
                                             'lineCount', coalesce(l_count.line_count, 0)))
       filter ( where l.id is not null )                                    as languages,

       jsonb_agg(distinct jsonb_build_object('id', e.id,
                                             'slug', e.slug,
                                             'name', e.name,
                                             'logoUrl', e.logo_url,
                                             'bannerUrl', e.banner_url,
                                             'url', e.url))
       filter ( where e.id is not null )                                    as ecosystems,

       jsonb_agg(distinct jsonb_build_object('id', prog.id,
                                             'name', prog.name,
                                             'logoUrl', prog.logo_url))
       filter ( where prog.id is not null )                                 as programs,

       count(distinct pgr.github_repo_id) > count(distinct agr.repo_id)     as has_repos_without_github_app_installed,

       dist_sum(distinct gr.id, gr.stars_count)                             as star_count,
       dist_sum(distinct gr.id, gr.forks_count)                             as fork_count,

       concat(coalesce(string_agg(distinct uleads.github_login, ' '), ''), ' ',
              coalesce(string_agg(distinct p.name, ' '), ''), ' ',
              coalesce(string_agg(distinct p.slug, ' '), ''), ' ',
              coalesce(string_agg(distinct pc.name, ' '), ''), ' ',
              coalesce(string_agg(distinct l.name, ' '), ''), ' ',
              coalesce(string_agg(distinct e.name, ' '), ''), ' ',
              coalesce(string_agg(distinct currencies.name, ' '), ''), ' ',
              coalesce(string_agg(distinct currencies.code, ' '), ''), ' ',
              coalesce(string_agg(distinct prog.name, ' '), ''))            as search
FROM projects p
         LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
         LEFT JOIN ecosystems e ON e.id = pe.ecosystem_id
         LEFT JOIN project_languages pl ON pl.project_id = p.id
         LEFT JOIN languages l ON l.id = pl.language_id
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
         LEFT JOIN project_categories pc ON pc.id = ppc.project_category_id
         LEFT JOIN v_programs_projects pp ON pp.project_id = p.id
         LEFT JOIN programs prog ON prog.id = pp.program_id
         LEFT JOIN project_leads pleads ON pleads.project_id = p.id
         LEFT JOIN iam.users uleads ON uleads.id = pleads.user_id
         LEFT JOIN pending_project_leader_invitations ppli ON ppli.project_id = p.id
         LEFT JOIN iam.users uinvleads ON uinvleads.github_user_id = ppli.github_user_id
         LEFT JOIN projects_tags pt ON pt.project_id = p.id
         LEFT JOIN project_github_repos pgr ON pgr.project_id = p.id
         LEFT JOIN indexer_exp.github_repos gr on gr.id = pgr.github_repo_id
         LEFT JOIN indexer_exp.authorized_github_repos agr on agr.repo_id = pgr.github_repo_id
         LEFT JOIN LATERAL (select distinct c.name, c.code
                            from bi.p_reward_data rd
                                     full outer join bi.p_project_grants_data gd on gd.project_id = rd.project_id
                                     join currencies c on c.id = coalesce(rd.currency_id, gd.currency_id)
                            where rd.project_id = p.id
                               or gd.project_id = p.id) currencies on true
          LEFT JOIN LATERAL (select pgr.project_id, grl.language, sum(grl.line_count) line_count
                            from project_github_repos pgr
                                     left join indexer_exp.github_repo_languages grl on grl.repo_id = pgr.github_repo_id
                            where pgr.project_id = p.id
                            group by pgr.project_id, grl.language) AS l_count on l_count.language = l.name
GROUP BY p.id) v;

call refresh_pseudo_projection('bi', 'project_global_data', 'project_id');

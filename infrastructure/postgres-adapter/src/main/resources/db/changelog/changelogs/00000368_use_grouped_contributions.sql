------------------------------------------------------------------------------------------------------------------------
-- Allows to get all indexed users a lot faster than by using all_users
CREATE VIEW iam.all_indexed_users AS
SELECT u.id                                                         AS user_id,
       ga.id                                                        AS github_user_id,
       ga.login                                                     AS login,
       COALESCE(upi.avatar_url, ga.avatar_url, u.github_avatar_url) AS avatar_url,
       u.email,
       COALESCE(upi.bio, ga.bio)                                    AS bio,
       u.created_at::timestamp with time zone                       AS signed_up_at,
       ga.created_at::timestamp with time zone                      AS signed_up_on_github_at
FROM indexer_exp.github_accounts ga
         LEFT JOIN iam.users u ON u.github_user_id = ga.id
         LEFT JOIN user_profile_info upi ON u.id = upi.id;


------------------------------------------------------------------------------------------------------------------------
-- Allows to select projects' programs fastly
CREATE MATERIALIZED VIEW m_programs_projects AS
SELECT *
FROM v_programs_projects;

create unique index on m_programs_projects (program_id, project_id);


------------------------------------------------------------------------------------------------------------------------
-- Put contribution_uuid into archived_github_contributions
delete
from archived_github_contributions;

alter table archived_github_contributions
    rename column id to contribution_uuid;

alter table archived_github_contributions
    alter column contribution_uuid type uuid using contribution_uuid::text::uuid;


------------------------------------------------------------------------------------------------------------------------
-- Put contribution_uuid into reward_items
create index on indexer_exp.grouped_contributions (coalesce(issue_id::text, pull_request_id::text, code_review_id));

alter table reward_items
    add column if not exists contribution_uuid uuid;

update reward_items
set contribution_uuid = (select c.contribution_uuid
                         from indexer_exp.grouped_contributions c
                         where coalesce(c.issue_id::text, c.pull_request_id::text, c.code_review_id) = reward_items.id);

update reward_items
set contribution_uuid = indexer.uuid_of(reward_items.id::text)
where contribution_uuid is null;

alter table reward_items
    alter column contribution_uuid set not null;

create unique index if not exists reward_items_contribution_uuid_index on reward_items (contribution_uuid, reward_id);
create unique index if not exists reward_items_contribution_uuid_index_inv on reward_items (reward_id, contribution_uuid);


------------------------------------------------------------------------------------------------------------------------
-- This hack allows us to force Postgres to use the right index when selecting rewards by contribution_uuid
CREATE OR REPLACE FUNCTION get_reward_ids_of_contributions(contributionUuids UUID[])
    RETURNS UUID[] AS
$$
select array_agg(distinct ri.reward_id)
from reward_items ri
where ri.contribution_uuid = any (contributionUuids)
$$ LANGUAGE SQL STABLE
                PARALLEL SAFE;


------------------------------------------------------------------------------------------------------------------------
call drop_pseudo_projection('bi', 'project_contributions_data');
call drop_pseudo_projection('bi', 'contribution_reward_data');
call drop_pseudo_projection('bi', 'contribution_data');


create type activity_status as enum ('ARCHIVED', 'NOT_ASSIGNED', 'IN_PROGRESS', 'TO_REVIEW', 'DONE');

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
       c.github_author_id                                                                                       as github_author_id,
       c.github_number                                                                                          as github_number,
       c.github_status                                                                                          as github_status,
       c.github_title                                                                                           as github_title,
       c.github_html_url                                                                                        as github_html_url,
       c.github_body                                                                                            as github_body,
       c.created_at                                                                                             as created_at,
       c.updated_at                                                                                             as updated_at,
       c.completed_at                                                                                           as completed_at,
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



call create_pseudo_projection('bi', 'contribution_contributors_data', $$

select c.contribution_uuid                                                                    as contribution_uuid,
       c.github_author_id                                                                     as github_author_id,
       array_agg(distinct gcc.contributor_id) filter ( where gcc.contributor_id is not null ) as contributor_ids,
       array_agg(distinct gia.user_id) filter ( where gia.user_id is not null )               as assignee_ids,
       array_agg(distinct a.applicant_id) filter ( where a.applicant_id is not null )         as applicant_ids,

       case when ad.contributor_id is not null then ad.contributor end                        as github_author,

       jsonb_agg(distinct cd.contributor) filter ( where cd.contributor_id is not null )      as contributors,

       jsonb_agg(distinct apd.contributor) filter ( where apd.contributor_id is not null )    as applicants,

       concat(c.github_number, ' ',
              c.github_title, ' ',
              ad.contributor_login, ' ',
              string_agg(distinct cd.contributor_login, ' '), ' ',
              string_agg(distinct apd.contributor_login, ' ')
       )                                                                                      as search
from indexer_exp.grouped_contributions c
         left join indexer_exp.grouped_contribution_contributors gcc on gcc.contribution_uuid = c.contribution_uuid
         left join bi.p_contributor_global_data cd on cd.contributor_id = gcc.contributor_id
         left join bi.p_contributor_global_data ad on ad.contributor_id = c.github_author_id
         left join indexer_exp.github_issues_assignees gia ON gia.issue_id = c.issue_id
         left join applications a on a.issue_id = c.issue_id
         left join bi.p_contributor_global_data apd on apd.contributor_id = a.applicant_id
group by c.contribution_uuid, ad.contributor_id
$$, 'contribution_uuid');

create index on bi.p_contribution_contributors_data using gin (contributor_ids);



call create_pseudo_projection('bi', 'per_contributor_contribution_data', $$
with first_contributions AS MATERIALIZED (select c.contributor_id, min(c.created_at) as first_contribution_date
                                          from indexer_exp.contributions c
                                          group by c.contributor_id)
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
       c.created_at = fc.first_contribution_date                          as is_first_contribution_on_onlydust,
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
         left join first_contributions fc on fc.contributor_id = cd.contributor_id
group by c.contribution_uuid,
         ccd.contribution_uuid,
         cd.contributor_id,
         u.id,
         fc.first_contribution_date
$$, 'technical_id');

create index on bi.p_per_contributor_contribution_data (contribution_uuid);
create index on bi.p_per_contributor_contribution_data (project_id);
create index on bi.p_per_contributor_contribution_data (project_slug);
create unique index on bi.p_per_contributor_contribution_data (contributor_id, contribution_uuid);
create unique index on bi.p_per_contributor_contribution_data (contributor_user_id, contribution_uuid);

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


call create_pseudo_projection('bi', 'contribution_reward_data', $$
select cd.contribution_uuid                                                       as contribution_uuid,
       cd.project_id                                                              as project_id,
       cd.repo_id                                                                 as repo_id,
       array_agg(distinct rd.reward_id) filter ( where rd.reward_id is not null ) as reward_ids,
       sum(round(rd.usd_amount, 2))                                               as total_rewarded_usd_amount
from bi.p_contribution_data cd
         join reward_items ri on ri.contribution_uuid = cd.contribution_uuid
         join bi.p_reward_data rd on rd.reward_id = ri.reward_id
group by cd.contribution_uuid, cd.project_id, cd.repo_id
$$, 'contribution_uuid');

create unique index on bi.p_contribution_reward_data (contribution_uuid, total_rewarded_usd_amount);
create unique index on bi.p_contribution_reward_data (repo_id, contribution_uuid);
create unique index on bi.p_contribution_reward_data (project_id, contribution_uuid);
create index on bi.p_contribution_reward_data using gin (reward_ids);


------------------------------------------------------------------------------------------------------------------------
-- Use m_programs_projects instead of v_programs_projects
CREATE OR REPLACE VIEW bi.v_contributor_global_data AS
SELECT v.*, md5(v::text) as hash
FROM (SELECT c.*,
             bi.search_of(c.contributor_login, c.projects, c.categories, c.languages, c.ecosystems, c.currencies, c.programs) as search

      FROM (SELECT c.*,
                   (select kyc.country
                    from iam.users u
                             join accounting.billing_profiles_users bpu on bpu.user_id = u.id
                             join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id and kyc.country is not null
                    where u.github_user_id = c.contributor_id
                    limit 1)                                    as contributor_country,

                   (select jsonb_build_object('githubUserId', u.github_user_id,
                                              'login', u.login,
                                              'avatarUrl', u.avatar_url,
                                              'isRegistered', u.user_id is not null,
                                              'id', u.user_id,
                                              'bio', u.bio,
                                              'signedUpAt', u.signed_up_at,
                                              'signedUpOnGithubAt', u.signed_up_on_github_at,
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
                                                                                               'contact', ci.contact))
                                                           from contact_informations ci
                                                           where ci.user_id = u.user_id)) as json

                    from iam.all_users u
                             left join global_users_ranks gur on gur.github_user_id = u.github_user_id
                    where u.github_user_id = c.contributor_id)  as contributor,

                   (select jsonb_agg(jsonb_build_object('id', p.id,
                                                        'slug', p.slug,
                                                        'name', p.name,
                                                        'logoUrl', p.logo_url))
                    from projects p
                    where p.id = any (c.project_ids))           as projects,

                   (select jsonb_agg(jsonb_build_object('id', l.id,
                                                        'slug', l.slug,
                                                        'name', l.name,
                                                        'logoUrl', l.logo_url,
                                                        'bannerUrl', l.banner_url))
                    from languages l
                    where l.id = any (c.language_ids))          as languages,

                   (select jsonb_agg(jsonb_build_object('id', e.id,
                                                        'slug', e.slug,
                                                        'name', e.name,
                                                        'logoUrl', e.logo_url,
                                                        'bannerUrl', e.banner_url,
                                                        'url', e.url))
                    from ecosystems e
                    where e.id = any (c.ecosystem_ids))         as ecosystems,

                   (select jsonb_agg(jsonb_build_object('id', pc.id,
                                                        'slug', pc.slug,
                                                        'name', pc.name,
                                                        'description', pc.description,
                                                        'iconSlug', pc.icon_slug))
                    from project_categories pc
                    where pc.id = any (c.project_category_ids)) as categories,

                   (select jsonb_agg(jsonb_build_object('id', prog.id,
                                                        'name', prog.name)) as json
                    from programs prog
                    where prog.id = any (c.program_ids))        as programs,

                   (select jsonb_agg(jsonb_build_object('id', cu.id,
                                                        'code', cu.code,
                                                        'name', cu.name)) as json
                    from currencies cu
                    where cu.id = any (c.currency_ids))         as currencies

            FROM (SELECT ga.id                                                                                            as contributor_id,
                         ga.login                                                                                         as contributor_login,

                         min(p.name)                                                                                      as first_project_name,

                         array_agg(distinct p.id) filter ( where p.id is not null )                                       as project_ids,
                         array_agg(distinct p.slug) filter ( where p.slug is not null )                                   as project_slugs,
                         array_agg(distinct ppc.project_category_id) filter ( where ppc.project_category_id is not null ) as project_category_ids,
                         array_agg(distinct lfe.language_id) filter ( where lfe.language_id is not null )                 as language_ids,
                         array_agg(distinct pe.ecosystem_id) filter ( where pe.ecosystem_id is not null )                 as ecosystem_ids,
                         array_agg(distinct pp.program_id) filter ( where pp.program_id is not null )                     as program_ids,
                         array_agg(distinct currencies.id) filter ( where currencies.id is not null )                     as currency_ids
                  FROM indexer_exp.github_accounts ga
                           LEFT JOIN indexer_exp.repos_contributors rc ON rc.contributor_id = ga.id
                           LEFT JOIN project_github_repos pgr ON pgr.github_repo_id = rc.repo_id
                           LEFT JOIN projects p on p.id = pgr.project_id
                           LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
                           LEFT JOIN m_programs_projects pp ON pp.project_id = p.id
                           LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
                           LEFT JOIN rewards r on r.recipient_id = ga.id
                           LEFT JOIN currencies on r.currency_id = currencies.id
                           LEFT JOIN indexer_exp.github_user_file_extensions gufe ON gufe.user_id = ga.id
                           LEFT JOIN language_file_extensions lfe ON lfe.extension = gufe.file_extension
                  GROUP BY ga.id) c) c) v;


------------------------------------------------------------------------------------------------------------------------
-- Use contribution_uuid instead of contribution_identity
-- Also update p_contribution_data to p_per_contributor_contribution_data
DROP FUNCTION bi.select_contributors(fromDate timestamptz,
                                     toDate timestamptz,
                                     dataSourceIds uuid[],
                                     contributorIds bigint[],
                                     contributedTo contribution_identity,
                                     projectIds uuid[],
                                     projectSlugs text[],
                                     categoryIds uuid[],
                                     languageIds uuid[],
                                     ecosystemIds uuid[],
                                     countryCodes text[],
                                     contributionStatuses indexer_exp.contribution_status[],
                                     searchQuery text,
                                     filteredKpis boolean);

drop type contribution_identity;

CREATE OR REPLACE FUNCTION bi.select_contributors(fromDate timestamptz,
                                                  toDate timestamptz,
                                                  dataSourceIds uuid[],
                                                  contributorIds bigint[],
                                                  contributedTo uuid[],
                                                  projectIds uuid[],
                                                  projectSlugs text[],
                                                  categoryIds uuid[],
                                                  languageIds uuid[],
                                                  ecosystemIds uuid[],
                                                  countryCodes text[],
                                                  contributionStatuses indexer_exp.contribution_status[],
                                                  searchQuery text,
                                                  filteredKpis boolean)
    RETURNS TABLE
            (
                contributor_id            bigint,
                contributor_login         text,
                contributor_country       text,
                contributor               jsonb,
                first_project_name        text,
                projects                  jsonb,
                categories                jsonb,
                languages                 jsonb,
                ecosystems                jsonb,
                total_rewarded_usd_amount numeric,
                reward_count              bigint,
                issue_count               bigint,
                pr_count                  bigint,
                code_review_count         bigint,
                contribution_count        bigint
            )
    STABLE
    PARALLEL SAFE
AS
$$
SELECT c.contributor_id                  as contributor_id,
       c.contributor_login               as contributor_login,
       c.contributor_country             as contributor_country,
       c.contributor                     as contributor,
       c.first_project_name              as first_project_name,
       c.projects                        as projects,
       c.categories                      as categories,
       c.languages                       as languages,
       c.ecosystems                      as ecosystems,
       sum(rd.total_rewarded_usd_amount) as total_rewarded_usd_amount,
       sum(rd.reward_count)              as reward_count,
       sum(cd.issue_count)               as issue_count,
       sum(cd.pr_count)                  as pr_count,
       sum(cd.code_review_count)         as code_review_count,
       sum(cd.contribution_count)        as contribution_count
FROM bi.p_contributor_global_data c

         LEFT JOIN (select cd.contributor_id,
                           count(cd.contribution_uuid)         as contribution_count,
                           coalesce(sum(cd.is_issue), 0)       as issue_count,
                           coalesce(sum(cd.is_pr), 0)          as pr_count,
                           coalesce(sum(cd.is_code_review), 0) as code_review_count
                    from bi.p_per_contributor_contribution_data cd
                    where (fromDate is null or cd.timestamp >= fromDate)
                      and (toDate is null or cd.timestamp < toDate)
                      and (dataSourceIds is null or cd.project_id = any (dataSourceIds) or
                           cd.program_ids && dataSourceIds or cd.ecosystem_ids && dataSourceIds)
                      and (contributionStatuses is null or cd.contribution_status = any (contributionStatuses))
                      and (not filteredKpis or projectIds is null or cd.project_id = any (projectIds))
                      and (not filteredKpis or projectSlugs is null or cd.project_slug = any (projectSlugs))
                      and (not filteredKpis or ecosystemIds is null or cd.ecosystem_ids && ecosystemIds)
                      and (not filteredKpis or categoryIds is null or cd.project_category_ids && categoryIds)
                      and (not filteredKpis or languageIds is null or cd.language_ids && languageIds)
                    group by cd.contributor_id) cd on cd.contributor_id = c.contributor_id

         LEFT JOIN (select rd.contributor_id,
                           count(rd.reward_id)             as reward_count,
                           coalesce(sum(rd.usd_amount), 0) as total_rewarded_usd_amount
                    from bi.p_reward_data rd
                    where (fromDate is null or rd.timestamp >= fromDate)
                      and (toDate is null or rd.timestamp < toDate)
                      and (dataSourceIds is null or rd.project_id = any (dataSourceIds) or
                           rd.program_ids && dataSourceIds or rd.ecosystem_ids && dataSourceIds)
                      and (not filteredKpis or projectIds is null or rd.project_id = any (projectIds))
                      and (not filteredKpis or projectSlugs is null or rd.project_slug = any (projectSlugs))
                      and (not filteredKpis or ecosystemIds is null or rd.ecosystem_ids && ecosystemIds)
                      and (not filteredKpis or categoryIds is null or rd.project_category_ids && categoryIds)
                      and (not filteredKpis or languageIds is null or rd.language_ids && languageIds)
                    group by rd.contributor_id) rd on rd.contributor_id = c.contributor_id

WHERE (dataSourceIds is null or c.project_ids && dataSourceIds or c.program_ids && dataSourceIds or
       c.ecosystem_ids && dataSourceIds)
  and (contributorIds is null or c.contributor_id = any (contributorIds))
  and (projectIds is null or c.project_ids && projectIds)
  and (projectSlugs is null or c.project_slugs && projectSlugs)
  and (ecosystemIds is null or c.ecosystem_ids && ecosystemIds)
  and (categoryIds is null or c.project_category_ids && categoryIds)
  and (languageIds is null or c.language_ids && languageIds)
  and (countryCodes is null or c.contributor_country = any (countryCodes))
  and (searchQuery is null or c.search ilike '%' || searchQuery || '%')
  and (contributedTo is null or exists(select 1
                                       from bi.p_per_contributor_contribution_data cd
                                       where cd.contributor_id = c.contributor_id
                                         and cd.contribution_uuid = any (contributedTo)))
  and (cd.contributor_id is not null or rd.contributor_id is not null)
GROUP BY c.contributor_id, c.contributor_login, c.contributor, c.first_project_name,
         c.projects, c.ecosystems,
         c.languages, c.categories, c.contributor_country;
$$
    LANGUAGE SQL;


CREATE OR REPLACE FUNCTION bi.select_projects(fromDate timestamptz,
                                              toDate timestamptz,
                                              dataSourceIds uuid[],
                                              programIds uuid[],
                                              projectIds uuid[],
                                              projectSlugs text[],
                                              projectLeadIds uuid[],
                                              categoryIds uuid[],
                                              languageIds uuid[],
                                              ecosystemIds uuid[],
                                              searchQuery text,
                                              showFilteredKpis boolean)
    RETURNS TABLE
            (
                project_id                  uuid,
                project_name                text,
                project                     jsonb,
                leads                       jsonb,
                categories                  jsonb,
                languages                   jsonb,
                ecosystems                  jsonb,
                programs                    jsonb,
                budget                      jsonb,
                available_budget_usd        numeric,
                percent_spent_budget_usd    numeric,
                total_granted_usd_amount    numeric,
                reward_count                bigint,
                issue_count                 bigint,
                pr_count                    bigint,
                code_review_count           bigint,
                contribution_count          bigint,
                total_rewarded_usd_amount   numeric,
                average_reward_usd_amount   numeric,
                active_contributor_count    bigint,
                onboarded_contributor_count bigint
            )
    STABLE
    PARALLEL RESTRICTED
AS
$$
SELECT p.project_id                        as project_id,
       p.project_name                      as project_name,
       p.project                           as project,
       p.leads                             as leads,
       p.categories                        as categories,
       p.languages                         as languages,
       p.ecosystems                        as ecosystems,
       p.programs                          as programs,
       pb.budget                           as budget,
       pb.available_budget_usd             as available_budget_usd,
       pb.percent_spent_budget_usd         as percent_spent_budget_usd,
       sum(gd.total_granted_usd_amount)    as total_granted_usd_amount,
       sum(rd.reward_count)                as reward_count,
       sum(cd.issue_count)                 as issue_count,
       sum(cd.pr_count)                    as pr_count,
       sum(cd.code_review_count)           as code_review_count,
       sum(cd.contribution_count)          as contribution_count,
       sum(rd.total_rewarded_usd_amount)   as total_rewarded_usd_amount,
       sum(rd.average_reward_usd_amount)   as average_reward_usd_amount,
       sum(cd.active_contributor_count)    as active_contributor_count,
       sum(cd.onboarded_contributor_count) as onboarded_contributor_count
FROM bi.p_project_global_data p
         JOIN bi.p_project_budget_data pb on pb.project_id = p.project_id

         LEFT JOIN (select cd.project_id,
                           count(cd.contribution_uuid)                                                             as contribution_count,
                           coalesce(sum(cd.is_issue), 0)                                                           as issue_count,
                           coalesce(sum(cd.is_pr), 0)                                                              as pr_count,
                           coalesce(sum(cd.is_code_review), 0)                                                     as code_review_count,
                           count(distinct cd.contributor_id)                                                       as active_contributor_count,
                           count(distinct cd.contributor_id) filter ( where cd.is_first_contribution_on_onlydust ) as onboarded_contributor_count
                    from bi.p_per_contributor_contribution_data cd
                    where cd.timestamp >= fromDate
                      and cd.timestamp < toDate
                      and (not showFilteredKpis or languageIds is null or cd.language_ids && languageIds)
                    group by cd.project_id) cd
                   on cd.project_id = p.project_id

         LEFT JOIN (select rd.project_id,
                           count(rd.reward_id)             as reward_count,
                           coalesce(sum(rd.usd_amount), 0) as total_rewarded_usd_amount,
                           coalesce(avg(rd.usd_amount), 0) as average_reward_usd_amount
                    from bi.p_reward_data rd
                    where rd.timestamp >= fromDate
                      and rd.timestamp < toDate
                      and (not showFilteredKpis or projectLeadIds is null or rd.requestor_id = any (projectLeadIds))
                      and (not showFilteredKpis or languageIds is null or rd.language_ids && languageIds)
                    group by rd.project_id) rd on rd.project_id = p.project_id

         LEFT JOIN (select gd.project_id,
                           coalesce(sum(gd.usd_amount), 0) as total_granted_usd_amount
                    from bi.p_project_grants_data gd
                    where gd.timestamp >= fromDate
                      and gd.timestamp < toDate
                    group by gd.project_id) gd on gd.project_id = p.project_id

WHERE (p.project_id = any (dataSourceIds) or p.program_ids && dataSourceIds or p.ecosystem_ids && dataSourceIds)
  and (ecosystemIds is null or p.ecosystem_ids && ecosystemIds)
  and (programIds is null or p.program_ids && programIds)
  and (projectIds is null or p.project_id = any (projectIds))
  and (projectSlugs is null or p.project_slug = any (projectSlugs))
  and (projectLeadIds is null or p.project_lead_ids && projectLeadIds)
  and (categoryIds is null or p.project_category_ids && categoryIds)
  and (languageIds is null or p.language_ids && languageIds)
  and (searchQuery is null or p.search ilike '%' || searchQuery || '%')
  and (cd.project_id is not null or rd.project_id is not null or gd.project_id is not null)

GROUP BY p.project_id, p.project_name, p.project, p.programs, p.ecosystems, p.languages, p.categories,
         p.leads, pb.budget, pb.available_budget_usd, pb.percent_spent_budget_usd
$$
    LANGUAGE SQL;
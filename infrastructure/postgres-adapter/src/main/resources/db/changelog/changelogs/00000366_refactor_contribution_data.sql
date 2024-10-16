create or replace view iam.all_users as
select u.id                                                         as user_id,
       coalesce(ga.id, u.github_user_id)                            as github_user_id,
       coalesce(ga.login, u.github_login)                           as login,
       coalesce(upi.avatar_url, ga.avatar_url, u.github_avatar_url) as avatar_url,
       u.email                                                      as email,
       coalesce(upi.bio, ga.bio)                                    as bio,
       u.created_at::timestamptz                                    as signed_up_at,
       ga.created_at::timestamptz                                   as signed_up_on_github_at
from iam.users u
         full outer join indexer_exp.github_accounts ga on ga.id = u.github_user_id
         left join user_profile_info upi on u.id = upi.id;



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
                           LEFT JOIN v_programs_projects pp ON pp.project_id = p.id
                           LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
                           LEFT JOIN rewards r on r.recipient_id = ga.id
                           LEFT JOIN currencies on r.currency_id = currencies.id
                           LEFT JOIN indexer_exp.github_user_file_extensions gufe ON gufe.user_id = ga.id
                           LEFT JOIN language_file_extensions lfe ON lfe.extension = gufe.file_extension
                  GROUP BY ga.id) c) c) v;



call drop_pseudo_projection('bi', 'project_contributions_data');
call drop_pseudo_projection('bi', 'contribution_data');


call create_pseudo_projection('bi', 'contribution_data', $$
with ranked_project_github_repos_relationship AS (SELECT *, row_number() OVER (PARTITION BY github_repo_id ORDER BY project_id) as row_number
                                                  FROM project_github_repos),
     first_contributions AS MATERIALIZED (select c.contributor_id, min(c.created_at) as first_contribution_date
                                          from indexer_exp.contributions c
                                          group by c.contributor_id)
select c.id                                                                                             as contribution_id,
       c.repo_id                                                                                        as repo_id,
       p.id                                                                                             as project_id,
       p.slug                                                                                           as project_slug,
       c.contributor_id                                                                                 as contributor_id,
       u.id                                                                                             as contributor_user_id,
       (array_agg(kyc.country) filter (where kyc.country is not null))[1]                               as contributor_country,
       c.created_at                                                                                     as timestamp,
       c.status                                                                                         as contribution_status,
       c.type                                                                                           as contribution_type,
       c.github_author_id                                                                               as github_author_id,
       coalesce(c.pull_request_id, c.issue_id, cr.pull_request_id)                                      as github_id,
       c.github_number                                                                                  as github_number,
       c.github_status                                                                                  as github_status,
       c.github_title                                                                                   as github_title,
       c.github_html_url                                                                                as github_html_url,
       c.github_body                                                                                    as github_body,
       c.pr_review_state                                                                                as github_code_review_state,
       c.created_at                                                                                     as created_at,
       c.updated_at                                                                                     as updated_at,
       c.completed_at                                                                                   as completed_at,
       date_trunc('day', c.created_at)                                                                  as day_timestamp,
       date_trunc('week', c.created_at)                                                                 as week_timestamp,
       date_trunc('month', c.created_at)                                                                as month_timestamp,
       date_trunc('quarter', c.created_at)                                                              as quarter_timestamp,
       date_trunc('year', c.created_at)                                                                 as year_timestamp,
       c.created_at = fc.first_contribution_date                                                        as is_first_contribution_on_onlydust,
       (c.type = 'ISSUE')::int                                                                          as is_issue,
       (c.type = 'PULL_REQUEST')::int                                                                   as is_pr,
       (c.type = 'CODE_REVIEW')::int                                                                    as is_code_review,
       array_agg(distinct lfe.language_id) filter ( where lfe.language_id is not null )                 as language_ids,
       array_agg(distinct pe.ecosystem_id) filter ( where pe.ecosystem_id is not null )                 as ecosystem_ids,
       array_agg(distinct pp.program_id) filter ( where pp.program_id is not null )                     as program_ids,
       array_agg(distinct ppc.project_category_id) filter ( where ppc.project_category_id is not null ) as project_category_ids,
       string_agg(distinct lfe.name, ' ')                                                               as languages,
       bool_or(gl.name ~~* '%good%first%issue%')                                                        as is_good_first_issue,
       array_agg(distinct gia.user_id) filter ( where gia.user_id is not null )                         as assignee_ids,
       array_agg(distinct gil.label_id) filter ( where gil.label_id is not null )                       as github_label_ids
from indexer_exp.contributions c
         left join ranked_project_github_repos_relationship pgr on pgr.github_repo_id = c.repo_id and pgr.row_number = 1
         left join projects p on p.id = pgr.project_id
         left join lateral ( select distinct lfe_1.language_id, l.name
                             from language_file_extensions lfe_1
                                      join languages l on l.id = lfe_1.language_id
                             where lfe_1.extension = any (c.main_file_extensions)) lfe on true
         left join projects_ecosystems pe on pe.project_id = p.id
         left join v_programs_projects pp on pp.project_id = p.id
         left join projects_project_categories ppc on ppc.project_id = p.id
         left join iam.users u on u.github_user_id = c.contributor_id
         left join accounting.billing_profiles_users bpu on bpu.user_id = u.id
         left join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id
         left join first_contributions fc on fc.contributor_id = c.contributor_id
         left join indexer_exp.github_issues_labels gil ON gil.issue_id = c.issue_id
         left join indexer_exp.github_labels gl ON gil.label_id = gl.id
         left join indexer_exp.github_issues_assignees gia ON gia.issue_id = c.issue_id
         left join indexer_exp.github_code_reviews cr on cr.id = c.code_review_id
group by c.id,
         c.repo_id,
         p.id,
         p.slug,
         c.contributor_id,
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
         u.id,
         fc.first_contribution_date
$$, 'contribution_id');

create index on bi.p_contribution_data (github_id);
create index on bi.p_contribution_data (project_id);
create index on bi.p_contribution_data (project_slug);
create unique index on bi.p_contribution_data (contributor_id, github_id, contribution_type);
create unique index on bi.p_contribution_data (contributor_user_id, github_id, contribution_type);

create index bi_contribution_data_repo_id_idx on bi.p_contribution_data (repo_id);

create index bi_contribution_data_project_id_timestamp_idx on bi.p_contribution_data (project_id, timestamp);
create index bi_contribution_data_project_id_day_timestamp_idx on bi.p_contribution_data (project_id, day_timestamp);
create index bi_contribution_data_project_id_week_timestamp_idx on bi.p_contribution_data (project_id, week_timestamp);
create index bi_contribution_data_project_id_month_timestamp_idx on bi.p_contribution_data (project_id, month_timestamp);
create index bi_contribution_data_project_id_quarter_timestamp_idx on bi.p_contribution_data (project_id, quarter_timestamp);
create index bi_contribution_data_project_id_year_timestamp_idx on bi.p_contribution_data (project_id, year_timestamp);
create index bi_contribution_data_project_id_timestamp_idx_inv on bi.p_contribution_data (timestamp, project_id);
create index bi_contribution_data_project_id_day_timestamp_idx_inv on bi.p_contribution_data (day_timestamp, project_id);
create index bi_contribution_data_project_id_week_timestamp_idx_inv on bi.p_contribution_data (week_timestamp, project_id);
create index bi_contribution_data_project_id_month_timestamp_idx_inv on bi.p_contribution_data (month_timestamp, project_id);
create index bi_contribution_data_project_id_quarter_timestamp_idx_inv on bi.p_contribution_data (quarter_timestamp, project_id);

create index bi_contribution_data_project_id_year_timestamp_idx_inv on bi.p_contribution_data (year_timestamp, project_id);
create index bi_contribution_data_contributor_id_timestamp_idx on bi.p_contribution_data (contributor_id, timestamp);
create index bi_contribution_data_contributor_id_day_timestamp_idx on bi.p_contribution_data (contributor_id, day_timestamp);
create index bi_contribution_data_contributor_id_week_timestamp_idx on bi.p_contribution_data (contributor_id, week_timestamp);
create index bi_contribution_data_contributor_id_month_timestamp_idx on bi.p_contribution_data (contributor_id, month_timestamp);
create index bi_contribution_data_contributor_id_quarter_timestamp_idx on bi.p_contribution_data (contributor_id, quarter_timestamp);
create index bi_contribution_data_contributor_id_year_timestamp_idx on bi.p_contribution_data (contributor_id, year_timestamp);
create index bi_contribution_data_contributor_id_timestamp_idx_inv on bi.p_contribution_data (timestamp, contributor_id);
create index bi_contribution_data_contributor_id_day_timestamp_idx_inv on bi.p_contribution_data (day_timestamp, contributor_id);
create index bi_contribution_data_contributor_id_week_timestamp_idx_inv on bi.p_contribution_data (week_timestamp, contributor_id);
create index bi_contribution_data_contributor_id_month_timestamp_idx_inv on bi.p_contribution_data (month_timestamp, contributor_id);
create index bi_contribution_data_contributor_id_quarter_timestamp_idx_inv on bi.p_contribution_data (quarter_timestamp, contributor_id);

create index bi_contribution_data_contributor_id_year_timestamp_idx_inv on bi.p_contribution_data (year_timestamp, contributor_id);


call create_pseudo_projection('bi', 'project_contributions_data', $$
SELECT p.id                                                                            as project_id,
       array_agg(distinct cd.repo_id)                                                  as repo_ids,
       count(distinct cd.contributor_id)                                               as contributor_count,
       count(cd.contribution_id) filter ( where cd.is_good_first_issue and
                                                coalesce(array_length(cd.assignee_ids, 1), 0) = 0 and
                                                cd.contribution_status != 'COMPLETED' and
                                                cd.contribution_status != 'CANCELLED') as good_first_issue_count
FROM projects p
         LEFT JOIN bi.p_contribution_data cd ON cd.project_id = p.id
GROUP BY p.id
$$, 'project_id');

create unique index on bi.p_project_contributions_data (project_id, contributor_count, good_first_issue_count);
create index on bi.p_project_contributions_data using gin (repo_ids);


call create_pseudo_projection('bi', 'contribution_reward_data', $$
select cd.contribution_id                                                         as contribution_id,
       cd.project_id                                                              as project_id,
       cd.repo_id                                                                 as repo_id,
       array_agg(distinct rd.reward_id) filter ( where rd.reward_id is not null ) as reward_ids,
       sum(round(rd.usd_amount, 2))                                               as total_rewarded_usd_amount
from bi.p_contribution_data cd
         left join reward_items ri on ri.type = cast(cast(cd.contribution_type as text) as contribution_type) and
                                      ri.repo_id = cd.repo_id and
                                      ri.number = cd.github_number
         left join bi.p_reward_data rd on rd.reward_id = ri.reward_id
group by cd.contribution_id, cd.project_id, cd.repo_id
$$, 'contribution_id');

create unique index on bi.p_contribution_reward_data (repo_id, contribution_id);
create unique index on bi.p_contribution_reward_data (project_id, contribution_id);
create index on bi.p_contribution_reward_data using gin (reward_ids);




CREATE TYPE contribution_identity AS
(
    github_id bigint,
    type      indexer_exp.contribution_type
);



DROP FUNCTION bi.select_contributors(fromDate timestamptz,
                                     toDate timestamptz,
                                     dataSourceIds uuid[],
                                     contributorIds bigint[],
                                     projectIds uuid[],
                                     projectSlugs text[],
                                     categoryIds uuid[],
                                     languageIds uuid[],
                                     ecosystemIds uuid[],
                                     countryCodes text[],
                                     contributionStatuses indexer_exp.contribution_status[],
                                     searchQuery text,
                                     filteredKpis boolean);

CREATE OR REPLACE FUNCTION bi.select_contributors(fromDate timestamptz,
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
                           count(cd.contribution_id)           as contribution_count,
                           coalesce(sum(cd.is_issue), 0)       as issue_count,
                           coalesce(sum(cd.is_pr), 0)          as pr_count,
                           coalesce(sum(cd.is_code_review), 0) as code_review_count
                    from bi.p_contribution_data cd
                    where (fromDate is null or cd.timestamp >= fromDate)
                      and (toDate is null or cd.timestamp < toDate)
                      and (dataSourceIds is null or cd.project_id = any (dataSourceIds) or cd.program_ids && dataSourceIds or cd.ecosystem_ids && dataSourceIds)
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
                      and (dataSourceIds is null or rd.project_id = any (dataSourceIds) or rd.program_ids && dataSourceIds or rd.ecosystem_ids && dataSourceIds)
                      and (not filteredKpis or projectIds is null or rd.project_id = any (projectIds))
                      and (not filteredKpis or projectSlugs is null or rd.project_slug = any (projectSlugs))
                      and (not filteredKpis or ecosystemIds is null or rd.ecosystem_ids && ecosystemIds)
                      and (not filteredKpis or categoryIds is null or rd.project_category_ids && categoryIds)
                      and (not filteredKpis or languageIds is null or rd.language_ids && languageIds)
                    group by rd.contributor_id) rd on rd.contributor_id = c.contributor_id

WHERE (dataSourceIds is null or c.project_ids && dataSourceIds or c.program_ids && dataSourceIds or c.ecosystem_ids && dataSourceIds)
  and (contributorIds is null or c.contributor_id = any (contributorIds))
  and (projectIds is null or c.project_ids && projectIds)
  and (projectSlugs is null or c.project_slugs && projectSlugs)
  and (ecosystemIds is null or c.ecosystem_ids && ecosystemIds)
  and (categoryIds is null or c.project_category_ids && categoryIds)
  and (languageIds is null or c.language_ids && languageIds)
  and (countryCodes is null or c.contributor_country = any (countryCodes))
  and (searchQuery is null or c.search ilike '%' || searchQuery || '%')
  and (contributedTo is null or exists(select 1
                                       from bi.p_contribution_data cd
                                       where cd.contributor_id = c.contributor_id
                                         and cd.github_id = contributedTo.github_id
                                         and cd.contribution_type = contributedTo.type))
  and (cd.contributor_id is not null or rd.contributor_id is not null)
GROUP BY c.contributor_id, c.contributor_login, c.contributor, c.first_project_name,
         c.projects, c.ecosystems,
         c.languages, c.categories, c.contributor_country;
$$
    LANGUAGE SQL;

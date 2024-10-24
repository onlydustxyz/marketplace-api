create type application_status as enum ('PENDING', 'ACCEPTED', 'SHELVED');

-- drop dependencies
call drop_pseudo_projection('bi', 'project_contributions_data');
call drop_pseudo_projection('bi', 'per_contributor_contribution_data');
call drop_pseudo_projection('bi', 'contribution_contributors_data');
call drop_pseudo_projection('bi', 'contributor_global_data');



------------------------------------------------------------------------------------------------------------------------
-- add maintained_project_ids
call create_pseudo_projection('bi', 'contributor_global_data', $$
SELECT c.*,
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
            GROUP BY ga.id, u.id) c) c
$$, 'contributor_id');



------------------------------------------------------------------------------------------------------------------------
-- restore dependencies
call create_pseudo_projection('bi', 'contribution_contributors_data', $$
select c.contribution_uuid                                                                    as contribution_uuid,
             c.repo_id                                                                              as repo_id,
             c.github_author_id                                                                     as github_author_id,
             array_agg(distinct gcc.contributor_id) filter ( where gcc.contributor_id is not null ) as contributor_ids,
             array_agg(distinct gia.user_id) filter ( where gia.user_id is not null )               as assignee_ids,
             array_agg(distinct a.applicant_id) filter ( where a.applicant_id is not null )         as applicant_ids,

             case when ad.contributor_id is not null then ad.contributor end                        as github_author,

             jsonb_agg(distinct jsonb_set(cd.contributor, '{since}', to_jsonb(gcc.tech_created_at::timestamptz), true))
             filter ( where cd.contributor_id is not null )                                         as contributors,

             jsonb_agg(distinct jsonb_set(jsonb_set(apd.contributor, '{since}', to_jsonb(a.received_at::timestamptz), true),
                                          '{applicationId}', to_jsonb(a.id), true))
             filter ( where apd.contributor_id is not null )                                        as applicants,

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

create index on bi.p_contribution_contributors_data (repo_id);
create index on bi.p_contribution_contributors_data using gin (contributor_ids);



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



------------------------------------------------------------------------------------------------------------------------
-- Create pseudo-projection for application_data
-- CAUTION depends on p_contribution_data and p_contribution_contributors_data
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


------------------------------------------------------------------------------------------------------------------------
DROP FUNCTION bi.select_contributors(fromDate timestamptz,
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
                                     filteredKpis boolean,
                                     includeApplicants boolean);


CREATE FUNCTION bi.select_contributors(fromDate timestamptz,
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
                                       filteredKpis boolean,
                                       includeApplicants boolean)
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
                maintained_projects       jsonb,
                total_rewarded_usd_amount numeric,
                reward_count              bigint,
                issue_count               bigint,
                pr_count                  bigint,
                code_review_count         bigint,
                contribution_count        bigint,
                in_progress_issue_count   bigint,
                pending_application_count bigint
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
       c.maintained_projects             as maintained_projects,
       sum(rd.total_rewarded_usd_amount) as total_rewarded_usd_amount,
       sum(rd.reward_count)              as reward_count,
       sum(cd.issue_count)               as issue_count,
       sum(cd.pr_count)                  as pr_count,
       sum(cd.code_review_count)         as code_review_count,
       sum(cd.contribution_count)        as contribution_count,
       sum(cd.in_progress_issue_count)   as in_progress_issue_count,
       sum(ad.pending_application_count) as pending_application_count
FROM bi.p_contributor_global_data c
         JOIN bi.p_contributor_reward_data crd ON crd.contributor_id = c.contributor_id
         JOIN bi.p_contributor_application_data cad ON cad.contributor_id = c.contributor_id

         LEFT JOIN (select cd.contributor_id,
                           count(cd.contribution_uuid)                                                           as contribution_count,
                           coalesce(sum(cd.is_issue), 0)                                                         as issue_count,
                           coalesce(sum(cd.is_pr), 0)                                                            as pr_count,
                           coalesce(sum(cd.is_code_review), 0)                                                   as code_review_count,
                           coalesce(sum(cd.is_issue) filter ( where cd.contribution_status = 'IN_PROGRESS' ), 0) as in_progress_issue_count
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

         LEFT JOIN (select ad.contributor_id,
                           count(ad.application_id)                                         as application_count,
                           count(ad.application_id) filter ( where ad.status = 'PENDING' )  as pending_application_count,
                           count(ad.application_id) filter ( where ad.status = 'ACCEPTED' ) as accepted_application_count,
                           count(ad.application_id) filter ( where ad.status = 'SHELVED' )  as shelved_application_count
                    from bi.p_application_data ad
                    where (fromDate is null or ad.timestamp >= fromDate)
                      and (toDate is null or ad.timestamp < toDate)
                      and (dataSourceIds is null or ad.project_id = any (dataSourceIds) or
                           ad.program_ids && dataSourceIds or ad.ecosystem_ids && dataSourceIds)
                      and (not filteredKpis or projectIds is null or ad.project_id = any (projectIds))
                      and (not filteredKpis or projectSlugs is null or ad.project_slug = any (projectSlugs))
                      and (not filteredKpis or ecosystemIds is null or ad.ecosystem_ids && ecosystemIds)
                      and (not filteredKpis or categoryIds is null or ad.project_category_ids && categoryIds)
                      and (not filteredKpis or languageIds is null or ad.language_ids && languageIds)
                    group by ad.contributor_id) ad on includeApplicants and ad.contributor_id = c.contributor_id

WHERE (dataSourceIds is null or
       c.contributed_on_project_ids && dataSourceIds or
       cad.applied_on_project_ids && dataSourceIds or
       c.program_ids && dataSourceIds or
       c.ecosystem_ids && dataSourceIds)
  and (contributorIds is null or c.contributor_id = any (contributorIds))
  and (projectIds is null or c.contributed_on_project_ids && projectIds or (includeApplicants and cad.applied_on_project_ids && projectIds))
  and (projectSlugs is null or c.contributed_on_project_slugs && projectSlugs or (includeApplicants and cad.applied_on_project_slugs && projectSlugs))
  and (ecosystemIds is null or c.ecosystem_ids && ecosystemIds)
  and (categoryIds is null or c.project_category_ids && categoryIds)
  and (languageIds is null or c.language_ids && languageIds)
  and (countryCodes is null or c.contributor_country = any (countryCodes))
  and (searchQuery is null or c.search ilike '%' || searchQuery || '%' or crd.search ilike '%' || searchQuery || '%')
  and (contributedTo is null or exists(select 1
                                       from bi.p_per_contributor_contribution_data cd
                                       where cd.contributor_id = c.contributor_id
                                         and cd.contribution_uuid = any (contributedTo)))
  and (cd.contributor_id is not null or rd.contributor_id is not null or ad.contributor_id is not null)
GROUP BY c.contributor_id, c.contributor_login, c.contributor, c.first_project_name,
         c.projects, c.ecosystems,
         c.languages, c.categories, c.contributor_country;
$$
    LANGUAGE SQL;

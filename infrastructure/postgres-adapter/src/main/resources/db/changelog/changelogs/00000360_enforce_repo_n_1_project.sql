-- A repo can only belong to one project:
CREATE UNIQUE INDEX project_github_repos_github_repo_id_idx
    ON public.project_github_repos (github_repo_id);



-- Refactor the view to use the project_github_repos table directly, as a repo can now only belong to one project
CREATE OR REPLACE VIEW bi.v_contribution_data AS
SELECT v.*, md5(v::text) as hash
FROM (with first_contributions AS MATERIALIZED (select c.contributor_id, min(c.created_at) as first_contribution_date
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
             string_agg(distinct lfe.name, ' ')                                                               as languages
      from indexer_exp.contributions c
               left join project_github_repos pgr on pgr.github_repo_id = c.repo_id
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
      group by c.id,
               c.repo_id,
               p.id,
               p.slug,
               c.contributor_id,
               c.created_at,
               c.type,
               c.status,
               u.id,
               fc.first_contribution_date) v;


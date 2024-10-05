CREATE FUNCTION bi.search_of(contributor_login text,
                             projects jsonb,
                             categories jsonb,
                             languages jsonb,
                             ecosystems jsonb,
                             currencies jsonb,
                             programs jsonb)
    RETURNS TEXT AS
$$
select concat(coalesce(string_agg(contributor_login, ' '), ''), ' ',
              coalesce((select concat(string_agg(p ->> 'name', ' '), ' ', string_agg(p ->> 'slug', ' ')) from jsonb_array_elements(projects) as p), ''), ' ',
              coalesce((select string_agg(pc ->> 'name', ' ') from jsonb_array_elements(categories) as pc), ''), ' ',
              coalesce((select string_agg(l ->> 'name', ' ') from jsonb_array_elements(languages) as l), ''), ' ',
              coalesce((select string_agg(e ->> 'name', ' ') from jsonb_array_elements(ecosystems) as e), ''), ' ',
              coalesce((select concat(string_agg(cu ->> 'name', ' '), ' ', string_agg(cu ->> 'code', ' ')) from jsonb_array_elements(currencies) as cu), ''),
              ' ',
              coalesce((select string_agg(prog ->> 'name', ' ') from jsonb_array_elements(programs) as prog), '')) as search
$$ LANGUAGE SQL STABLE;



CREATE OR REPLACE VIEW bi.v_contributor_global_data AS
SELECT v.*, md5(v::text) as hash
FROM (SELECT c.*,
             bi.search_of(c.contributor_login, c.projects, c.categories, c.languages, c.ecosystems, c.currencies, c.programs) as search

      FROM (SELECT c.*,

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

            FROM (WITH ranked_project_github_repos_relationship AS (SELECT project_github_repos.project_id,
                                                                           project_github_repos.github_repo_id,
                                                                           row_number()
                                                                           OVER (PARTITION BY project_github_repos.github_repo_id ORDER BY project_github_repos.project_id) AS row_number
                                                                    FROM project_github_repos),
                       user_countries as (select bpu.user_id as user_id,
                                                 kyc.country as country
                                          from accounting.billing_profiles_users bpu
                                                   join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id)
                  SELECT u.github_user_id                                                                                 as contributor_id,
                         u.login                                                                                          as contributor_login,
                         uc.country                                                                                       as contributor_country,
                         jsonb_build_object('githubUserId', u.github_user_id,
                                            'login', u.login,
                                            'avatarUrl', u.avatar_url,
                                            'isRegistered', u.user_id is not null,
                                            'id',
                                            u.user_id)                                                                    as contributor,

                         min(p.name)                                                                                      as first_project_name,

                         array_agg(distinct p.id) filter ( where p.id is not null )                                       as project_ids,
                         array_agg(distinct p.slug) filter ( where p.slug is not null )                                   as project_slugs,
                         array_agg(distinct ppc.project_category_id) filter ( where ppc.project_category_id is not null ) as project_category_ids,
                         array_agg(distinct lfe.language_id) filter ( where lfe.language_id is not null )                 as language_ids,
                         array_agg(distinct pe.ecosystem_id) filter ( where pe.ecosystem_id is not null )                 as ecosystem_ids,
                         array_agg(distinct pp.program_id) filter ( where pp.program_id is not null )                     as program_ids,
                         array_agg(distinct currencies.id) filter ( where currencies.id is not null )                     as currency_ids
                  FROM iam.all_users u
                           LEFT JOIN user_countries uc on uc.user_id = u.user_id
                           LEFT JOIN indexer_exp.contributions c ON c.contributor_id = u.github_user_id
                           LEFT JOIN ranked_project_github_repos_relationship pgr ON pgr.github_repo_id = c.repo_id AND pgr.row_number = 1
                           LEFT JOIN projects p on p.id = pgr.project_id
                           LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
                           LEFT JOIN v_programs_projects pp ON pp.project_id = p.id
                           LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
                           LEFT JOIN rewards r on r.recipient_id = u.github_user_id
                           LEFT JOIN currencies on r.currency_id = currencies.id
                           LEFT JOIN LATERAL ( SELECT DISTINCT lfe_1.language_id,
                                                               l.name
                                               FROM language_file_extensions lfe_1
                                                        JOIN languages l ON l.id = lfe_1.language_id
                                               WHERE lfe_1.extension = ANY (c.main_file_extensions)) lfe ON true
                  GROUP BY u.github_user_id,
                           u.login,
                           u.avatar_url,
                           u.user_id,
                           uc.country) c) c) v;



CREATE OR REPLACE VIEW bi.v_contribution_data AS
SELECT v.*, md5(v::text) as hash
FROM (with ranked_project_github_repos_relationship AS (SELECT *, row_number() OVER (PARTITION BY github_repo_id ORDER BY project_id) as row_number
                                                        FROM project_github_repos)
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
             (select not exists(select 1
                                from indexer_exp.contributions cc
                                where cc.contributor_id = c.contributor_id
                                  and cc.created_at < c.created_at))                                          as is_first_contribution_on_onlydust,
             (c.type = 'ISSUE')::int                                                                          as is_issue,
             (c.type = 'PULL_REQUEST')::int                                                                   as is_pr,
             (c.type = 'CODE_REVIEW')::int                                                                    as is_code_review,
             array_agg(distinct lfe.language_id) filter ( where lfe.language_id is not null )                 as language_ids,
             array_agg(distinct pe.ecosystem_id) filter ( where pe.ecosystem_id is not null )                 as ecosystem_ids,
             array_agg(distinct pp.program_id) filter ( where pp.program_id is not null )                     as program_ids,
             array_agg(distinct ppc.project_category_id) filter ( where ppc.project_category_id is not null ) as project_category_ids,
             string_agg(distinct lfe.name, ' ')                                                               as languages
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
      group by c.id,
               c.repo_id,
               p.id,
               p.slug,
               c.contributor_id,
               c.created_at,
               c.type,
               c.status,
               u.id) v;

create index on indexer_exp.contributions (contributor_id, created_at);

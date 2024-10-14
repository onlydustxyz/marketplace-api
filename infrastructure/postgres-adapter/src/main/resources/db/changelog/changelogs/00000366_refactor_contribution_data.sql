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
                                              'globalRank', gur.rank,
                                              'globalRankPercentile', gur.rank_percentile,
                                              'globalRankCategory', case
                                                                        when gur.rank_percentile <= 0.02 then 'A'
                                                                        when gur.rank_percentile <= 0.04 then 'B'
                                                                        when gur.rank_percentile <= 0.06 then 'C'
                                                                        when gur.rank_percentile <= 0.08 then 'D'
                                                                        when gur.rank_percentile <= 0.10 then 'E'
                                                                        else 'F'
                                                  end)
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


CREATE OR REPLACE VIEW bi.v_contributor_global_data AS
SELECT v.*, md5(v::text) as hash
FROM (WITH user_countries as (select bpu.user_id as user_id,
                                     kyc.country as country
                              from accounting.billing_profiles_users bpu
                                       join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id)
      SELECT u.github_user_id                                                                                    as contributor_id,
             u.login                                                                                             as contributor_login,
             uc.country                                                                                          as contributor_country,
             jsonb_build_object('githubUserId', u.github_user_id,
                                'login', u.login,
                                'avatarUrl', u.avatar_url,
                                'isRegistered', u.user_id is not null,
                                'id',
                                u.user_id)                                                                       as contributor,

             min(p.name)                                                                                         as first_project_name,

             jsonb_agg(distinct jsonb_build_object('id', p.id,
                                                   'slug', p.slug,
                                                   'name', p.name,
                                                   'logoUrl', p.logo_url)) filter ( where p.id is not null )     as projects,

             array_agg(distinct p.id) filter ( where p.id is not null )                                          as project_ids,
             array_agg(distinct p.slug) filter ( where p.slug is not null )                                      as project_slugs,
             array_agg(distinct pc.id) filter ( where pc.id is not null )                                        as project_category_ids,
             array_agg(distinct l.id) filter ( where l.id is not null )                                          as language_ids,
             array_agg(distinct e.id) filter ( where e.id is not null )                                          as ecosystem_ids,
             array_agg(distinct prog.id) filter ( where prog.id is not null )                                    as program_ids,

             jsonb_agg(distinct jsonb_build_object('id', pc.id,
                                                   'slug', pc.slug,
                                                   'name', pc.name,
                                                   'description', pc.description,
                                                   'iconSlug', pc.icon_slug))
             filter ( where pc.id is not null )                                                                  as categories,

             jsonb_agg(distinct jsonb_build_object('id', l.id,
                                                   'slug', l.slug,
                                                   'name', l.name,
                                                   'logoUrl', l.logo_url,
                                                   'bannerUrl', l.banner_url)) filter ( where l.id is not null ) as languages,

             jsonb_agg(distinct jsonb_build_object('id', e.id,
                                                   'slug', e.slug,
                                                   'name', e.name,
                                                   'logoUrl', e.logo_url,
                                                   'bannerUrl', e.banner_url,
                                                   'url', e.url))
             filter ( where e.id is not null )                                                                   as ecosystems,

             concat(coalesce(string_agg(distinct u.login, ' '), ''), ' ',
                    coalesce(string_agg(distinct p.name, ' '), ''), ' ',
                    coalesce(string_agg(distinct p.slug, ' '), ''), ' ',
                    coalesce(string_agg(distinct pc.name, ' '), ''), ' ',
                    coalesce(string_agg(distinct l.name, ' '), ''), ' ',
                    coalesce(string_agg(distinct e.name, ' '), ''), ' ',
                    coalesce(string_agg(distinct currencies.name, ' '), ''), ' ',
                    coalesce(string_agg(distinct currencies.code, ' '), ''), ' ',
                    coalesce(string_agg(distinct prog.name, ' '), ''))                                           as search
      FROM iam.all_users u
               LEFT JOIN user_countries uc on uc.user_id = u.user_id
               LEFT JOIN bi.v_contribution_data cd on cd.contributor_id = u.github_user_id
               LEFT JOIN projects p on p.id = cd.project_id
               LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
               LEFT JOIN ecosystems e ON e.id = pe.ecosystem_id
               LEFT JOIN v_programs_projects pp ON pp.project_id = p.id
               LEFT JOIN programs prog ON prog.id = pp.program_id
               LEFT JOIN languages l ON l.id = any (cd.language_ids)
               LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
               LEFT JOIN project_categories pc ON pc.id = ppc.project_category_id
               LEFT JOIN rewards r on r.recipient_id = u.github_user_id
               LEFT JOIN currencies on r.currency_id = currencies.id
      GROUP BY u.github_user_id,
               u.login,
               u.avatar_url,
               u.user_id,
               uc.country) v;
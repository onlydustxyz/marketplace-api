CREATE MATERIALIZED VIEW bi.contributor_global_data AS
WITH user_contries as (select bpu.user_id as user_id,
                              kyc.country as country
                       from accounting.billing_profiles_users bpu
                                join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id),
     project_programs AS (select distinct abt.program_id,
                                          abt.project_id
                          from accounting.account_book_transactions abt
                          where abt.project_id is not null
                            and abt.reward_id is null),
     user_languages as (select u.github_user_id                   as contributor_id,
                               unnest(upi.preferred_language_ids) as language_id
                        from user_profile_info upi
                                 join iam.users u on upi.id = u.id
                        union
                        select cd.contributor_id       as contributor_id,
                               unnest(cd.language_ids) as language_id
                        from bi.contribution_data cd),
     project_users as
         (select pl.project_id    as project_id,
                 u.github_user_id as github_user_id,
                 'MAINTAINER'     as role
          from project_leads pl
                   join iam.users u on pl.user_id = u.id
          union
          select cd.project_id     as project_id,
                 cd.contributor_id as github_user_id,
                 'CONTRIBUTOR'     as role
          from bi.contribution_data cd
          union
          select r.project_id   as project_id,
                 r.recipient_id as github_user_id,
                 'CONTRIBUTOR'  as role
          from rewards r
          union
          select abt.project_id   as project_id,
                 u.github_user_id as github_user_id,
                 'PROGRAM_LEAD'   as role
          from accounting.account_book_transactions abt
                   join program_leads pl on pl.program_id = abt.program_id
                   join iam.users u on pl.user_id = u.id
          where abt.project_id is not null)
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

       array_agg(distinct pu.role)                                                                         as roles,

       jsonb_agg(distinct jsonb_build_object('id', p.id,
                                             'slug', p.slug,
                                             'name', p.name,
                                             'logoUrl', p.logo_url)) filter ( where p.id is not null )     as projects,

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
         LEFT JOIN user_contries uc on uc.user_id = u.user_id
         LEFT JOIN project_users pu on pu.github_user_id = u.github_user_id
         LEFT JOIN projects p on p.id = pu.project_id
         LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
         LEFT JOIN ecosystems e ON e.id = pe.ecosystem_id
         LEFT JOIN project_programs pp ON pp.project_id = p.id
         LEFT JOIN programs prog ON prog.id = pp.program_id
         LEFT JOIN user_languages ul ON ul.contributor_id = u.github_user_id
         LEFT JOIN languages l ON l.id = ul.language_id
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
         LEFT JOIN project_categories pc ON pc.id = ppc.project_category_id
         LEFT JOIN rewards r on r.recipient_id = u.github_user_id
         LEFT JOIN currencies on r.currency_id = currencies.id
GROUP BY u.github_user_id, u.login, u.avatar_url, u.user_id, uc.country;


CREATE UNIQUE INDEX bi_contributor_global_data_pk ON bi.contributor_global_data (contributor_id);


CREATE FUNCTION bi.select_contributors(fromDate timestamptz,
                                       toDate timestamptz,
                                       programOrEcosystemIds uuid[],
                                       categoryIds uuid[],
                                       languageIds uuid[],
                                       ecosystemIds uuid[],
                                       countryCodes text[],
                                       contributorRoles text[],
                                       searchQuery text)
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
                merged_pr_count           bigint,
                reward_count              bigint,
                contribution_count        bigint
            )
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
       sum(cd.merged_pr_count)           as merged_pr_count,
       sum(rd.reward_count)              as reward_count,
       sum(cd.contribution_count)        as contribution_count
FROM bi.contributor_global_data c

         LEFT JOIN (select cd.contributor_id,
                           count(cd.contribution_id)         as contribution_count,
                           coalesce(sum(cd.is_merged_pr), 0) as merged_pr_count
                    from bi.contribution_data cd
                    where cd.timestamp >= fromDate
                      and cd.timestamp < toDate
                      and (languageIds is null or cd.language_ids && languageIds)
                    group by cd.contributor_id) cd on cd.contributor_id = c.contributor_id

         LEFT JOIN (select rd.contributor_id,
                           count(rd.reward_id)             as reward_count,
                           coalesce(sum(rd.usd_amount), 0) as total_rewarded_usd_amount
                    from bi.reward_data rd
                    where rd.timestamp >= fromDate
                      and rd.timestamp < toDate
                    group by rd.contributor_id) rd on rd.contributor_id = c.contributor_id

WHERE (c.program_ids && programOrEcosystemIds or c.ecosystem_ids && programOrEcosystemIds)
  and (ecosystemIds is null or c.ecosystem_ids && ecosystemIds)
  and (categoryIds is null or c.project_category_ids && categoryIds)
  and (languageIds is null or c.language_ids && languageIds)
  and (countryCodes is null or c.contributor_country = any (countryCodes))
  and (searchQuery is null or c.search ilike '%' || searchQuery || '%')
  and (contributorRoles is null or c.roles && contributorRoles)
  and (cd.contributor_id is not null or rd.contributor_id is not null or
       array ['MAINTAINER', 'PROGRAM_LEAD'] && c.roles)

GROUP BY c.contributor_id, c.contributor_login, c.contributor, c.first_project_name, c.projects, c.ecosystems,
         c.languages, c.categories, c.contributor_country;
$$
    LANGUAGE SQL;
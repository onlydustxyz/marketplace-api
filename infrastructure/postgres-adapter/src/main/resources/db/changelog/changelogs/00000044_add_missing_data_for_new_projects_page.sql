update languages
set name = 'TypeScript'
where name = 'Typescript';

update languages
set name = 'JavaScript'
where name = 'Javascript';

call drop_pseudo_projection('bi', 'project_contributions_data');

call create_pseudo_projection('bi', 'project_contributions_data', $$
SELECT p.id                                                             as project_id,
       count(distinct unnested.contributor_ids)                         as contributor_count,
       count(distinct cd.contribution_uuid) filter ( where cd.is_good_first_issue and
                                                           coalesce(array_length(ccd.assignee_ids, 1), 0) = 0 and
                                                           cd.contribution_status != 'COMPLETED' and
                                                           cd.contribution_status !=
                                                           'CANCELLED') as good_first_issue_count,
       count(distinct cd.contribution_uuid) filter ( where cd.contribution_type = 'ISSUE' and
                                                           coalesce(array_length(ccd.assignee_ids, 1), 0) = 0 and
                                                           cd.contribution_status != 'COMPLETED' and
                                                           cd.contribution_status !=
                                                           'CANCELLED') as available_issue_count,
       count(distinct cd.contribution_uuid) filter ( where gl.id is not null and
                                                           coalesce(array_length(ccd.assignee_ids, 1), 0) = 0 and
                                                           cd.contribution_status != 'COMPLETED' and
                                                           cd.contribution_status !=
                                                           'CANCELLED') as live_hackathon_issue_count,
       (select sum(gr.forks_count)
        from project_github_repos pgr
                 join indexer_exp.github_repos gr on pgr.github_repo_id = gr.id
        where p.id = pgr.project_id)                                       fork_count,
       (select sum(gr.stars_count)
        from project_github_repos pgr
                 join indexer_exp.github_repos gr on pgr.github_repo_id = gr.id
        where p.id = pgr.project_id)                                       star_count
FROM projects p
         LEFT JOIN bi.p_contribution_data cd ON cd.project_id = p.id
         LEFT JOIN bi.p_contribution_contributors_data ccd ON ccd.contribution_uuid = cd.contribution_uuid
         LEFT JOIN unnest(ccd.contributor_ids) unnested(contributor_ids) ON true
         LEFT JOIN hackathon_projects hp ON hp.project_id = p.id
         LEFT JOIN hackathons h ON h.id = hp.hackathon_id AND
                                   h.status = 'PUBLISHED' AND
                                   h.start_date <= now() AND
                                   h.end_date >= now()
         LEFT JOIN indexer_exp.github_labels gl ON gl.name = any (h.github_labels) AND gl.id = any (cd.github_label_ids)
GROUP BY p.id
$$, 'project_id');

create unique index on bi.p_project_contributions_data (project_id, contributor_count, good_first_issue_count, live_hackathon_issue_count);

call drop_pseudo_projection('bi', 'project_global_data');

call create_pseudo_projection('bi', 'project_global_data', $$
SELECT p.id                                                                 as project_id,
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
                                             'lineCount', l.line_count))
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
         LEFT JOIN indexer_exp.authorized_github_repos agr on agr.repo_id = pgr.github_repo_id
         LEFT JOIN LATERAL (select distinct c.name, c.code
                            from bi.p_reward_data rd
                                     full outer join bi.p_project_grants_data gd on gd.project_id = rd.project_id
                                     join currencies c on c.id = coalesce(rd.currency_id, gd.currency_id)
                            where rd.project_id = p.id
                               or gd.project_id = p.id) currencies on true
         LEFT JOIN LATERAL (
    select l.name, l.id, l.slug, l.logo_url, l.banner_url, sum(grp.line_count) line_count
    from project_github_repos pgr
             join indexer_exp.github_repo_languages grp on grp.repo_id = pgr.github_repo_id
             join project_languages pl on pl.project_id = pgr.project_id
             join languages l on l.name = grp.language
    where pgr.project_id = p.id
    group by l.name, l.id, l.slug, l.logo_url, l.banner_url
    ) l on true
GROUP BY p.id
$$, 'project_id');

create unique index on bi.p_project_global_data (project_slug);

create unique index on languages (name);
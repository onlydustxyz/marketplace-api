package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectPageItemQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectsPageRepository extends JpaRepository<ProjectPageItemQueryEntity, UUID> {

    @Query(value = """
            select p.id,
                   p.hiring,
                   p.logo_url,
                   p.slug,
                   p.name,
                   p.short_description,
                   'PUBLIC' as visibility,
                   r_count.repo_count  as repo_count,
                   coalesce(pc_count.contributors_count,0)                as contributors_count,
                   (select count(pl_count.user_id)
                    from project_leads pl_count
                    where pl_count.project_id = p.id) as project_lead_count,
                   false                                      as   is_pending_project_lead,
                   (select json_agg(jsonb_build_object(
                           'id', pl.user_id,
                           'githubId', u.github_user_id,
                           'login', COALESCE(ga.login, u.github_login),
                           'avatarUrl', user_avatar_url(u.github_user_id, COALESCE(ga.avatar_url, u.github_avatar_url)),
                           'url', ga.html_url
                           ))
                    from project_leads pl
                         left join iam.users u on u.id = pl.user_id
                         left join indexer_exp.github_accounts ga on ga.id = u.github_user_id
                    where pl.project_id = p.id
                    group by pl.project_id)                   as   project_leads,
                   t.technologies as  technologies,
                   s.ecosystem_json                                 ecosystems,
                   tags.names                                    tags,
                   coalesce(languages.json, '[]')   as languages
            from public_projects p
                left join ((select pt.project_id, jsonb_agg(jsonb_build_object(pt.technology, pt.line_count)) technologies
                            from project_technologies pt
                            group by pt.project_id)) as t on t.project_id = p.id
                left join (select ps.project_id,
                                           jsonb_agg(jsonb_build_object(
                                                   'url', ecosystem.url,
                                                   'logoUrl', ecosystem.logo_url,
                                                   'id', ecosystem.id,
                                                   'name', ecosystem.name,
                                                   'slug', ecosystem.slug,
                                                   'hidden', ecosystem.hidden
                                           )) ecosystem_json
                                    from ecosystems ecosystem
                                             join public.projects_ecosystems ps on ps.ecosystem_id = ecosystem.id
                                    group by ps.project_id) s on s.project_id = p.id
                left join (select pgr_count.project_id, count(github_repo_id) repo_count
                                from project_github_repos pgr_count
                                join indexer_exp.github_repos gr on gr.id = pgr_count.github_repo_id
                                where gr.visibility = 'PUBLIC'
                                group by pgr_count.project_id) r_count on r_count.project_id = p.id
                left join (select pc_count.project_id, count(pc_count.github_user_id) as contributors_count
                                    from public.projects_contributors pc_count
                                    group by pc_count.project_id) pc_count on pc_count.project_id = p.id
                left join (select p_tags.project_id, jsonb_agg(jsonb_build_object('name', p_tags.tag)) names
                                    from projects_tags p_tags
                                    group by p_tags.project_id) tags on tags.project_id = p.id
                left join (select pl.project_id,
                                  jsonb_agg(jsonb_build_object(
                                            'id', l.id,
                                            'name', l.name,
                                            'slug', l.slug,
                                            'logoUrl', l.logo_url,
                                            'bannerUrl', l.banner_url
                                            )) json
                                  from project_languages pl
                           join languages l on pl.language_id = l.id
                           group by pl.project_id) languages on languages.project_id = p.id
                left join (select pgfi.project_id, count(*) > 0 exist
                                    from projects_good_first_issues pgfi
                                    group by pgfi.project_id) has_good_first_issues on has_good_first_issues.project_id = p.id
            where r_count.repo_count > 0
              and (coalesce(:languagesJsonPath) is null or jsonb_path_exists(languages.json, cast(cast(:languagesJsonPath as text) as jsonpath )))
              and (coalesce(:ecosystemsJsonPath) is null or jsonb_path_exists(s.ecosystem_json, cast(cast(:ecosystemsJsonPath as text) as jsonpath )))
              and (coalesce(:tagsJsonPath) is null or jsonb_path_exists(tags.names, cast(cast(:tagsJsonPath as text) as jsonpath )))
              and (coalesce(:categorySlugs) is null or exists(select 1 from projects_project_categories ppc
                                                                       join project_categories pc on ppc.project_category_id = pc.id
                                                                       where ppc.project_id = p.id and pc.slug in (:categorySlugs)))
              and (coalesce(:search) is null or p.name ilike '%' || cast(:search as text) ||'%' or p.short_description ilike '%' || cast(:search as text) ||'%')
              and (coalesce(:hasGoodFirstIssues) is null or has_good_first_issues.exist = :hasGoodFirstIssues)
              order by case
                           when cast(:orderBy as text) = 'NAME' then (upper(p.name), 0)
                           when cast(:orderBy as text) = 'REPO_COUNT' then (-r_count.repo_count, upper(p.name))
                           when cast(:orderBy as text) = 'CONTRIBUTOR_COUNT' then (-pc_count.contributors_count, upper(p.name))
                           when cast(:orderBy as text) = 'RANK' then (-p.rank, upper(p.name))
                       end
              offset :offset limit :limit
            """, nativeQuery = true)
    List<ProjectPageItemQueryEntity> findProjectsForAnonymousUser(@Param("search") String search,
                                                                  @Param("tagsJsonPath") String tagsJsonPath,
                                                                  @Param("ecosystemsJsonPath") String ecosystemsJsonPath,
                                                                  @Param("languagesJsonPath") String languagesJsonPath,
                                                                  @Param("categorySlugs") List<String> categorySlugs,
                                                                  @Param("hasGoodFirstIssues") Boolean hasGoodFirstIssues,
                                                                  @Param("orderBy") String orderBy,
                                                                  @Param("offset") int offset,
                                                                  @Param("limit") int limit);

    @Query(value = """
            select p.id,
                   p.hiring,
                   p.logo_url,
                   p.slug,
                   p.name,
                   p.short_description,
                   p.visibility,
                   r_count.repo_count                         as repo_count,
                   coalesce(pc_count.contributors_count, 0)   as contributors_count,
                   pl_count.project_lead_count as project_lead_count,
                   (select json_agg(jsonb_build_object(
                           'id', pl.user_id,
                           'githubId', u.github_user_id,
                           'login', COALESCE(ga.login, u.github_login),
                           'avatarUrl', user_avatar_url(u.github_user_id, COALESCE(ga.avatar_url, u.github_avatar_url)),
                           'url', ga.html_url
                           ))
                    from project_leads pl
                             join iam.users u on u.id = pl.user_id
                             left join indexer_exp.github_accounts ga on ga.id = u.github_user_id
                    where pl.project_id = p.id
                    group by pl.project_id)                     as project_leads,
                   t.technologies                               as technologies,
                   s.ecosystem_json                               as ecosystems,
                   coalesce(is_pending_pl.is_p_pl, false)       as is_pending_project_lead,
                   tags.names                                    tags,
                   coalesce(languages.json, '[]')   as languages
            from projects p
                     left join ((select pt.project_id, jsonb_agg(jsonb_build_object(pt.technology, pt.line_count)) technologies
                                 from project_technologies pt
                                 group by pt.project_id)) as t on t.project_id = p.id
                     left join (select ps.project_id,
                                       jsonb_agg(jsonb_build_object(
                                               'url', ecosystem.url,
                                               'logoUrl', ecosystem.logo_url,
                                               'id', ecosystem.id,
                                               'name', ecosystem.name,
                                               'slug', ecosystem.slug,
                                               'hidden', ecosystem.hidden
                                       )) ecosystem_json
                                from ecosystems ecosystem
                                join public.projects_ecosystems ps on ps.ecosystem_id = ecosystem.id
                                group by ps.project_id) s on s.project_id = p.id
                     left join (select pgr_count.project_id, count(github_repo_id) repo_count
                                from project_github_repos pgr_count
                                join indexer_exp.github_repos gr3 on gr3.id = pgr_count.github_repo_id
                                where gr3.visibility ='PUBLIC'
                                group by pgr_count.project_id) r_count on r_count.project_id = p.id
                     left join (select pc_count.project_id, count(pc_count.github_user_id) as contributors_count
                                from public.projects_contributors pc_count
                                group by pc_count.project_id) pc_count on pc_count.project_id = p.id
                     left join (select pl_me.project_id, case count(*) when 0 then false else true end is_lead
                                from project_leads pl_me
                                where pl_me.user_id = :userId
                                group by pl_me.project_id) is_me_lead on is_me_lead.project_id = p.id
                     left join (select ppc.project_id, case count(*) when 0 then false else true end is_p_c
                                from projects_pending_contributors ppc
                                         left join iam.users me on me.github_user_id = ppc.github_user_id
                                where me.id = :userId
                                group by ppc.project_id) is_pending_contributor on is_pending_contributor.project_id = p.id
                     left join (select ppli.project_id, case count(*) when 0 then false else true end is_p_pl
                                from pending_project_leader_invitations ppli
                                         left join iam.users me on me.github_user_id = ppli.github_user_id
                                where me.id = :userId
                                group by ppli.project_id) is_pending_pl on is_pending_pl.project_id = p.id
                     left join (select pl_count.project_id, count(pl_count.user_id) project_lead_count
                                from project_leads pl_count
                                group by pl_count.project_id) pl_count on pl_count.project_id = p.id
                     left join (select p_tags.project_id, jsonb_agg(jsonb_build_object('name', p_tags.tag)) names
                                    from projects_tags p_tags
                                    group by p_tags.project_id) tags on tags.project_id = p.id
                     left join (select pl.project_id,
                                  jsonb_agg(jsonb_build_object(
                                            'id', l.id,
                                            'name', l.name,
                                            'slug', l.slug,
                                            'logoUrl', l.logo_url,
                                            'bannerUrl', l.banner_url
                                            )) json
                                  from project_languages pl
                           join languages l on pl.language_id = l.id
                           group by pl.project_id) languages on languages.project_id = p.id
                    left join (select pgfi.project_id, count(*) > 0 exist
                                        from projects_good_first_issues pgfi
                                        group by pgfi.project_id) has_good_first_issues on has_good_first_issues.project_id = p.id
                    left join project_recommendations pr on pr.project_id = p.id and pr.user_id = :userId
            where r_count.repo_count > 0
              and (p.visibility = 'PUBLIC' 
                       or (p.visibility = 'PRIVATE' 
                                and (pl_count.project_lead_count > 0 or coalesce(is_pending_pl.is_p_pl, false))
                                and (coalesce(is_pending_pl.is_p_pl, false) or
                                     coalesce(is_me_lead.is_lead, false) or 
                                     coalesce(is_pending_contributor.is_p_c, false))
                          )
                  )
              and (coalesce(:languagesJsonPath) is null or jsonb_path_exists(languages.json, cast(cast(:languagesJsonPath as text) as jsonpath )))
              and (coalesce(:ecosystemsJsonPath) is null or jsonb_path_exists(s.ecosystem_json, cast(cast(:ecosystemsJsonPath as text) as jsonpath)))
              and (coalesce(:tagsJsonPath) is null or jsonb_path_exists(tags.names, cast(cast(:tagsJsonPath as text) as jsonpath )))
              and (coalesce(:categorySlugs) is null or exists(select 1 from projects_project_categories ppc
                                                                       join project_categories pc on ppc.project_category_id = pc.id
                                                                       where ppc.project_id = p.id and pc.slug in (:categorySlugs)))
              and (coalesce(:search) is null or p.name ilike '%' || cast(:search as text) || '%' or p.short_description ilike '%' || cast(:search as text) || '%')
              and (coalesce(:mine) is null or case when :mine is true then (coalesce(is_me_lead.is_lead, false) or coalesce(is_pending_pl.is_p_pl, false)) else true end)
              and (coalesce(:hasGoodFirstIssues) is null or has_good_first_issues.exist = :hasGoodFirstIssues)
            order by case
                         when cast(:orderBy as text) = 'NAME' then (not coalesce(is_pending_pl.is_p_pl, false), upper(p.name), 0)
                         when cast(:orderBy as text) = 'REPO_COUNT' then (not coalesce(is_pending_pl.is_p_pl, false),-r_count.repo_count,upper(p.name))
                         when cast(:orderBy as text) = 'CONTRIBUTOR_COUNT' then (not coalesce(is_pending_pl.is_p_pl, false), -pc_count.contributors_count, upper(p.name))
                         when cast(:orderBy as text) = 'RANK' then (not coalesce(is_pending_pl.is_p_pl, false), coalesce(-pr.score, -p.rank), upper(p.name))
                     end
                     offset :offset limit :limit
            """, nativeQuery = true)
    List<ProjectPageItemQueryEntity> findProjectsForUserId(@Param("userId") UUID userId,
                                                           @Param("mine") Boolean mine,
                                                           @Param("search") String search,
                                                           @Param("tagsJsonPath") String tagsJsonPath,
                                                           @Param("ecosystemsJsonPath") String ecosystemsJsonPath,
                                                           @Param("languagesJsonPath") String languagesJsonPath,
                                                           @Param("categorySlugs") List<String> categorySlugs,
                                                           @Param("hasGoodFirstIssues") Boolean hasGoodFirstIssues,
                                                           @Param("orderBy") String orderBy,
                                                           @Param("offset") int offset,
                                                           @Param("limit") int limit);

    @Query(value = """
                        select count(p.id)
                        from public_projects p
                        left join ((select pt.project_id, jsonb_agg(jsonb_build_object(pt.technology, pt.line_count)) technologies
                                    from project_technologies pt
                                    group by pt.project_id)) as t on t.project_id = p.id
                        left join (select ps.project_id,
                                                   jsonb_agg(jsonb_build_object(
                                                           'url', ecosystem.url,
                                                           'logoUrl', ecosystem.logo_url,
                                                           'id', ecosystem.id,
                                                           'name', ecosystem.name,
                                                           'slug', ecosystem.hidden,
                                                           'hidden', ecosystem.slug
                                                   )) ecosystem_json
                                            from ecosystems ecosystem
                                                     join public.projects_ecosystems ps on ps.ecosystem_id = ecosystem.id
                                            group by ps.project_id) s on s.project_id = p.id
                        left join (select p_tags.project_id, jsonb_agg(jsonb_build_object('name', p_tags.tag)) names
                                    from projects_tags p_tags
                                    group by p_tags.project_id) tags on tags.project_id = p.id
                        left join (select pl.project_id,
                                  jsonb_agg(jsonb_build_object(
                                            'id', l.id,
                                            'name', l.name,
                                            'slug', l.slug,
                                            'logoUrl', l.logo_url,
                                            'bannerUrl', l.banner_url
                                            )) json
                                  from project_languages pl
                           join languages l on pl.language_id = l.id
                           group by pl.project_id) languages on languages.project_id = p.id
                        left join (select pgfi.project_id, count(*) > 0 exist
                                            from projects_good_first_issues pgfi
                                            group by pgfi.project_id) has_good_first_issues on has_good_first_issues.project_id = p.id
                        where (select count(github_repo_id)
                                           from project_github_repos pgr_count
                                           join indexer_exp.github_repos gr2 on gr2.id = pgr_count.github_repo_id
                                           where pgr_count.project_id = p.id and gr2.visibility = 'PUBLIC') > 0
                                       and (coalesce(:languagesJsonPath) is null or jsonb_path_exists(languages.json, cast(cast(:languagesJsonPath as text) as jsonpath )))
                                       and (coalesce(:ecosystemsJsonPath) is null or jsonb_path_exists(s.ecosystem_json, cast(cast(:ecosystemsJsonPath as text) as jsonpath )))
                                       and (coalesce(:tagsJsonPath) is null or jsonb_path_exists(tags.names, cast(cast(:tagsJsonPath as text) as jsonpath )))
                                       and (coalesce(:categorySlugs) is null or exists(select 1 from projects_project_categories ppc
                                                                                                join project_categories pc on ppc.project_category_id = pc.id
                                                                                                where ppc.project_id = p.id and pc.slug in (:categorySlugs)))
                                       and (coalesce(:search) is null or p.name ilike '%' || cast(:search as text) ||'%' or p.short_description ilike '%' || cast(:search as text) ||'%')
                                       and (coalesce(:hasGoodFirstIssues) is null or has_good_first_issues.exist = :hasGoodFirstIssues)
            """
            , nativeQuery = true)
    Long countProjectsForAnonymousUser(@Param("search") String search,
                                       @Param("tagsJsonPath") String tagsJsonPath,
                                       @Param("ecosystemsJsonPath") String ecosystemsJsonPath,
                                       @Param("languagesJsonPath") String languagesJsonPath,
                                       @Param("categorySlugs") List<String> categorySlugs,
                                       @Param("hasGoodFirstIssues") Boolean hasGoodFirstIssues);

    @Query(value = """
            select count(p.id)
            from projects p
                     left join ((select pt.project_id, jsonb_agg(jsonb_build_object(pt.technology, pt.line_count)) technologies
                                 from project_technologies pt
                                 group by pt.project_id)) as t on t.project_id = p.id
                     left join (select ps.project_id,
                                       jsonb_agg(jsonb_build_object(
                                               'url', ecosystem.url,
                                               'logoUrl', ecosystem.logo_url,
                                               'id', ecosystem.id,
                                               'name', ecosystem.name,
                                               'slug', ecosystem.slug,
                                               'hidden', ecosystem.hidden
                                       )) ecosystem_json
                                from ecosystems ecosystem
                                join public.projects_ecosystems ps on ps.ecosystem_id = ecosystem.id
                                group by ps.project_id) s on s.project_id = p.id
                     left join (select pgr_count.project_id, count(github_repo_id) repo_count
                                from project_github_repos pgr_count
                                join indexer_exp.github_repos gr2 on gr2.id = pgr_count.github_repo_id
                                where gr2.visibility = 'PUBLIC'
                                group by pgr_count.project_id) r_count on r_count.project_id = p.id
                     left join (select pc_count.project_id, count(pc_count.github_user_id) as contributors_count
                                from public.projects_contributors pc_count
                                group by pc_count.project_id) pc_count on pc_count.project_id = p.id
                     left join (select pl_me.project_id, case count(*) when 0 then false else true end is_lead
                                from project_leads pl_me
                                where pl_me.user_id = :userId
                                group by pl_me.project_id) is_me_lead on is_me_lead.project_id = p.id
                     left join (select ppc.project_id, case count(*) when 0 then false else true end is_p_c
                                from projects_pending_contributors ppc
                                         left join iam.users me on me.github_user_id = ppc.github_user_id
                                where me.id = :userId
                                group by ppc.project_id) is_pending_contributor on is_pending_contributor.project_id = p.id
                     left join (select ppli.project_id, case count(*) when 0 then false else true end is_p_pl
                                from pending_project_leader_invitations ppli
                                         left join iam.users me on me.github_user_id = ppli.github_user_id
                                where me.id = :userId
                                group by ppli.project_id) is_pending_pl on is_pending_pl.project_id = p.id
                     left join (select pl_count.project_id, count(pl_count.user_id) project_lead_count
                                from project_leads pl_count
                                group by pl_count.project_id) pl_count on pl_count.project_id = p.id
                    left join (select p_tags.project_id, jsonb_agg(jsonb_build_object('name', p_tags.tag)) names
                                    from projects_tags p_tags
                                    group by p_tags.project_id) tags on tags.project_id = p.id
                    left join (select pl.project_id,
                                  jsonb_agg(jsonb_build_object(
                                            'id', l.id,
                                            'name', l.name,
                                            'slug', l.slug,
                                            'logoUrl', l.logo_url,
                                            'bannerUrl', l.banner_url
                                            )) json
                                  from project_languages pl
                           join languages l on pl.language_id = l.id
                           group by pl.project_id) languages on languages.project_id = p.id
                    left join (select pgfi.project_id, count(*) > 0 exist
                                        from projects_good_first_issues pgfi
                                        group by pgfi.project_id) has_good_first_issues on has_good_first_issues.project_id = p.id
            where r_count.repo_count > 0
              and (p.visibility = 'PUBLIC'
                or (p.visibility = 'PRIVATE' and (pl_count.project_lead_count > 0 or coalesce(is_pending_pl.is_p_pl, false))
                    and (coalesce(is_pending_pl.is_p_pl, false) or
                         coalesce(is_me_lead.is_lead, false) or coalesce(is_pending_contributor.is_p_c, false))))
              and (coalesce(:languagesJsonPath) is null or jsonb_path_exists(languages.json, cast(cast(:languagesJsonPath as text) as jsonpath )))
              and (coalesce(:ecosystemsJsonPath) is null or jsonb_path_exists(s.ecosystem_json, cast(cast(:ecosystemsJsonPath as text) as jsonpath)))
              and (coalesce(:tagsJsonPath) is null or jsonb_path_exists(tags.names, cast(cast(:tagsJsonPath as text) as jsonpath )))
              and (coalesce(:categorySlugs) is null or exists(select 1 from projects_project_categories ppc
                                                                       join project_categories pc on ppc.project_category_id = pc.id
                                                                       where ppc.project_id = p.id and pc.slug in (:categorySlugs)))
              and (coalesce(:search) is null or p.name ilike '%' || cast(:search as text) || '%' or
                   p.short_description ilike '%' || cast(:search as text) || '%')
              and (coalesce(:mine) is null or case when :mine is true then (coalesce(is_me_lead.is_lead, false) or coalesce(is_pending_pl.is_p_pl, false)) else true end)
              and (coalesce(:hasGoodFirstIssues) is null or has_good_first_issues.exist = :hasGoodFirstIssues)
            """, nativeQuery = true)
    Long countProjectsForUserId(@Param("userId") UUID userId,
                                @Param("mine") Boolean mine,
                                @Param("search") String search,
                                @Param("tagsJsonPath") String tagsJsonPath,
                                @Param("ecosystemsJsonPath") String ecosystemsJsonPath,
                                @Param("languagesJsonPath") String languagesJsonPath,
                                @Param("categorySlugs") List<String> categorySlugs,
                                @Param("hasGoodFirstIssues") Boolean hasGoodFirstIssues);
}

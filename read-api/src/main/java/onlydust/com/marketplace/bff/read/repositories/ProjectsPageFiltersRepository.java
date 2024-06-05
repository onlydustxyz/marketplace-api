package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.project.ProjectPageItemFiltersQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectsPageFiltersRepository extends JpaRepository<ProjectPageItemFiltersQueryEntity, UUID> {

    @Query(value = """
            select p.id,
                   coalesce(s.ecosystem_json , '[]') as ecosystems,
                   coalesce(l.language_json  , '[]') as languages,
                   coalesce(cat.category_json, '[]') as categories
            from projects p
                     left join (select ps.project_id,
                                       jsonb_agg(jsonb_build_object(
                                               'url', ecosystem.url,
                                               'logoUrl', ecosystem.logo_url,
                                               'id', ecosystem.id,
                                               'name', ecosystem.name,
                                               'slug', ecosystem.slug
                                                 )) ecosystem_json
                                from ecosystems ecosystem
                                         join public.projects_ecosystems ps on ps.ecosystem_id = ecosystem.id
                                group by ps.project_id) s on s.project_id = p.id
                    left join (select pl.project_id, jsonb_agg(jsonb_build_object(
                                                               'id', l.id,
                                                               'name', l.name,
                                                               'slug', l.slug,
                                                               'logoUrl', l.logo_url,
                                                               'bannerUrl', l.banner_url
                                                    )) language_json
                               from languages l
                                        join project_languages pl on pl.language_id = l.id
                                        group by pl.project_id) l on l.project_id = p.id
                    left join (select ppc.project_id, jsonb_agg(jsonb_build_object(
                                                               'id', pc.id,
                                                               'slug', pc.slug,
                                                               'name', pc.name,
                                                               'iconSlug', pc.icon_slug
                                                    )) category_json
                               from project_categories pc
                                        join projects_project_categories ppc on ppc.project_category_id = pc.id
                                        group by ppc.project_id) cat on cat.project_id = p.id
            where (select count(github_repo_id)
                   from project_github_repos pgr_count
                   join indexer_exp.github_repos gr on pgr_count.github_repo_id = gr.id
                   where pgr_count.project_id = p.id
                   and gr.visibility = 'PUBLIC') > 0
              and p.visibility = 'PUBLIC'""",
            nativeQuery = true)
    List<ProjectPageItemFiltersQueryEntity> findFiltersForAnonymousUser();

    @Query(value = """
            select p.id,
                   coalesce(s.ecosystem_json , '[]') as ecosystems,
                   coalesce(l.language_json  , '[]') as languages,
                   coalesce(cat.category_json, '[]') as categories
            from projects p
                     left join (select ps.project_id,
                                       jsonb_agg(jsonb_build_object(
                                               'url', ecosystem.url,
                                               'logoUrl', ecosystem.logo_url,
                                               'id', ecosystem.id,
                                               'name', ecosystem.name,
                                               'slug', ecosystem.slug
                                                 )) ecosystem_json
                                from ecosystems ecosystem
                                         join public.projects_ecosystems ps on ps.ecosystem_id = ecosystem.id
                                group by ps.project_id) s on s.project_id = p.id
                     left join (select pl.project_id, jsonb_agg(jsonb_build_object(
                                                               'id', l.id,
                                                               'name', l.name,
                                                               'slug', l.slug,
                                                               'logoUrl', l.logo_url,
                                                               'bannerUrl', l.banner_url
                                                    )) language_json
                               from languages l
                                        join project_languages pl on pl.language_id = l.id
                                        group by pl.project_id) l on l.project_id = p.id
                    left join (select ppc.project_id, jsonb_agg(jsonb_build_object(
                                                               'id', pc.id,
                                                               'slug', pc.slug,
                                                               'name', pc.name,
                                                               'iconSlug', pc.icon_slug
                                                    )) category_json
                               from project_categories pc
                                        join projects_project_categories ppc on ppc.project_category_id = pc.id
                                        group by ppc.project_id) cat on cat.project_id = p.id
                     left join (select pgr_count.project_id, count(github_repo_id) repo_count
                                from project_github_repos pgr_count
                                group by pgr_count.project_id) r_count on r_count.project_id = p.id
                     left join (select pc_count.project_id, count(pc_count.github_user_id) as contributors_count
                                from public.projects_contributors pc_count
                                group by pc_count.project_id) pc_count on pc_count.project_id = p.id
                     left join (select pl_me.project_id, case count(*) when 0 then false else true end is_lead
                                from project_leads pl_me
                                where pl_me.user_id = :userId
                                group by pl_me.project_id) is_me_lead on is_me_lead.project_id = p.id
                     left join (select pc_me.project_id, case count(*) when 0 then false else true end is_c
                                from projects_contributors pc_me
                                         left join iam.users me on me.github_user_id = pc_me.github_user_id
                                where me.id = :userId
                                group by pc_me.project_id) is_contributor on is_contributor.project_id = p.id
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
            where r_count.repo_count > 0
              and (p.visibility = 'PUBLIC'
                or (p.visibility = 'PRIVATE' and (pl_count.project_lead_count > 0 or coalesce(is_pending_pl.is_p_pl, false))
                    and (coalesce(is_contributor.is_c, false) or coalesce(is_pending_pl.is_p_pl, false) or
                         coalesce(is_me_lead.is_lead, false) or coalesce(is_pending_contributor.is_p_c, false))))
              and (coalesce(:mine) is null or case when :mine is true then (coalesce(is_me_lead.is_lead, false) or coalesce(is_pending_pl.is_p_pl, false)) else true end)
                     """, nativeQuery = true)
    List<ProjectPageItemFiltersQueryEntity> findFiltersForUser(@Param("userId") UUID userId,
                                                               @Param("mine") Boolean mine);
}

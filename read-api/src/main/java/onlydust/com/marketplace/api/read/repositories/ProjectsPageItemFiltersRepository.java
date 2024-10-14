package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectPageItemFiltersQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProjectsPageItemFiltersRepository extends JpaRepository<ProjectPageItemFiltersQueryEntity, Long> {

    @Query(value = """
            SELECT 1                                                as id,
                   (select jsonb_agg(distinct jsonb_build_object('id', pc.id,
                                                                 'slug', pc.slug,
                                                                 'name', pc.name,
                                                                 'description', pc.description,
                                                                 'iconSlug', pc.icon_slug))
                    from project_categories pc
                    where pc.id = any (d.agg_project_category_ids)) as categories,
            
                   (select jsonb_agg(distinct jsonb_build_object('id', l.id,
                                                                 'slug', l.slug,
                                                                 'name', l.name,
                                                                 'logoUrl', l.logo_url,
                                                                 'bannerUrl', l.banner_url))
                    from languages l
                    where l.id = any (d.agg_language_ids))          as languages,
            
                   (select jsonb_agg(distinct jsonb_build_object('id', e.id,
                                                                 'slug', e.slug,
                                                                 'name', e.name,
                                                                 'logoUrl', e.logo_url,
                                                                 'bannerUrl', e.banner_url,
                                                                 'url', e.url))
                    from ecosystems e
                    where e.id = any (d.agg_ecosystem_ids))         as ecosystems
            FROM (SELECT array_agg(distinct unnested_project_category_ids) filter ( where unnested_project_category_ids is not null ) as agg_project_category_ids,
                         array_agg(distinct unnested_language_ids) filter ( where unnested_language_ids is not null )                 as agg_language_ids,
                         array_agg(distinct unnested_ecosystem_ids) filter ( where unnested_ecosystem_ids is not null )               as agg_ecosystem_ids
                  FROM bi.p_project_global_data p
                           LEFT JOIN unnest(p.project_category_ids) AS unnested_project_category_ids ON true
                           LEFT JOIN unnest(p.language_ids) AS unnested_language_ids ON true
                           LEFT JOIN unnest(p.ecosystem_ids) AS unnested_ecosystem_ids ON true
                  WHERE (p.project_visibility = 'PUBLIC' and array_length(p.repo_ids, 1) > 0 or
                         :userId = any (p.project_lead_ids) or
                         :userId = any (p.invited_project_lead_ids))) d
            """, nativeQuery = true)
    ProjectPageItemFiltersQueryEntity findFilters(UUID userId);
}

package onlydust.com.marketplace.api.read.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import onlydust.com.marketplace.api.read.entities.project.ProjectV2ReadEntity;

public interface ProjectsV2ReadRepository extends JpaRepository<ProjectV2ReadEntity, UUID> {

    @Query(value = """
            SELECT p.project_id                                as id,
                   p.project_slug                              as slug,
                   p.project_name                              as name,
                   p.project ->> 'shortDescription'            as short_description,
                   p.project ->> 'logoUrl'                     as logo_url,
                   coalesce(pcd.contributor_count, 0)          as contributor_count,
                   p.categories                                as categories,
                   p.languages                                 as languages,
                   p.tags                                      as tags,
                   coalesce(p.ecosystems, '[]')                as ecosystems,
                   coalesce(pcd.good_first_issue_count, 0)     as good_first_issue_count,
                   coalesce(pcd.available_issue_count, 0)      as available_issue_count,
                   coalesce(p.star_count, 0)                   as star_count,
                   coalesce(p.fork_count, 0)                   as fork_count
            FROM bi.p_project_global_data p
                     JOIN bi.p_project_contributions_data pcd on p.project_id = pcd.project_id
            WHERE (p.project_visibility = 'PUBLIC' and array_length(p.repo_ids, 1) > 0)
              and (cast(:ecosystemIds as uuid[]) is null or p.ecosystem_ids && cast(:ecosystemIds as uuid[]))
              and (cast(:projectIds as uuid[]) is null or p.project_id = any (cast(:projectIds as uuid[])))
              and (cast(:categoryIds as uuid[]) is null or p.project_category_ids && cast(:categoryIds as uuid[]))
              and (cast(:languageIds as uuid[]) is null or p.language_ids && cast(:languageIds as uuid[]))
              and (cast(:tags as project_tag[]) is null or p.tags && cast(:tags as project_tag[]))
            """, nativeQuery = true)
    Page<ProjectV2ReadEntity> findAll(UUID[] projectIds,
                                               UUID[] categoryIds,
                                               UUID[] languageIds,
                                               UUID[] ecosystemIds,
                                               String[] tags,
                                               Pageable pageable);


    @Query(value = """
            SELECT pd.project_id                                         as id,
                   pd.project_slug                                       as slug,
                   pd.project_name                                       as name,
                   pd.project ->> 'shortDescription'                     as short_description,
                   p.long_description                                    as long_description,
                   pd.project ->> 'logoUrl'                              as logo_url,
                   coalesce(pcd.contributor_count, 0)                    as contributor_count,
                   pd.categories                                         as categories,
                   pd.languages                                          as languages,
                   pd.tags                                               as tags,
                   coalesce(pd.ecosystems, '[]')                         as ecosystems,
                   coalesce(pd.leads, '[]')                              as leads,
                   coalesce(pcd.good_first_issue_count, 0)               as good_first_issue_count,
                   coalesce(pcd.available_issue_count, 0)                as available_issue_count,
                   coalesce(pd.star_count, 0)                            as star_count,
                   coalesce(pd.fork_count, 0)                            as fork_count,
                   coalesce(pcd.merged_pr_count, 0)                      as merged_pr_count,
                   coalesce(pcd.current_week_available_issue_count, 0)   as current_week_available_issue_count,
                   coalesce(pcd.current_week_merged_pr_count, 0)         as current_week_merged_pr_count,

                   (select jsonb_agg(jsonb_build_object('url', pmi.url, 'value', pmi.name) order by pmi.rank)
                   from project_more_infos pmi 
                   where pmi.project_id = pd.project_id)        as more_infos
            FROM bi.p_project_global_data pd
                     JOIN projects p on pd.project_id = p.id
                     JOIN bi.p_project_contributions_data pcd on pd.project_id = pcd.project_id
            WHERE pd.project_id = :id or pd.project_slug = :slug
            """, nativeQuery = true)
    Optional<ProjectV2ReadEntity> findByIdOrSlug(UUID id, String slug);
}

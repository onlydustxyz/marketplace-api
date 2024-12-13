package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectPageV2ItemQueryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProjectsPageV2Repository extends JpaRepository<ProjectPageV2ItemQueryEntity, UUID> {

    @Query(value = """
            SELECT p.project_id                                                                  as id,
                   p.project_slug                                                                as slug,
                   p.project_name                                                                as name,
                   p.project ->> 'shortDescription'                                              as short_description,
                   p.project ->> 'logoUrl'                                                       as logo_url,
                   coalesce(pcd.contributor_count, 0)                                            as contributor_count,
                   p.categories                                                                  as categories,
                   p.languages                                                                   as languages,
                   p.tags                                                                        as tags,
                   coalesce(pcd.good_first_issue_count, 0)                                       as good_first_issue_count,
                   coalesce(pcd.available_issue_count, 0)                                        as available_issue_count,
                   coalesce(p.star_count, 0)                                                   as star_count,
                   coalesce(p.fork_count, 0)                                                   as fork_count
            FROM bi.p_project_global_data p
                     JOIN bi.p_project_contributions_data pcd on p.project_id = pcd.project_id
            WHERE (p.project_visibility = 'PUBLIC' and array_length(p.repo_ids, 1) > 0)
              and (cast(:ecosystemIds as uuid[]) is null or p.ecosystem_ids && cast(:ecosystemIds as uuid[]))
              and (cast(:projectIds as uuid[]) is null or p.project_id = any (cast(:projectIds as uuid[])))
              and (cast(:categoryIds as uuid[]) is null or p.project_category_ids && cast(:categoryIds as uuid[]))
              and (cast(:languageIds as uuid[]) is null or p.language_ids && cast(:languageIds as uuid[]))
              and (cast(:tags as project_tag[]) is null or p.tags && cast(:tags as project_tag[]))
            """, nativeQuery = true)
    Page<ProjectPageV2ItemQueryEntity> findAll(UUID[] projectIds,
                                               UUID[] categoryIds,
                                               UUID[] languageIds,
                                               UUID[] ecosystemIds,
                                               String[] tags,
                                               Pageable pageable);
}

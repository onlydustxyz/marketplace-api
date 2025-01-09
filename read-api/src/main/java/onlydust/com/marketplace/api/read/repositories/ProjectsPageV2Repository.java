package onlydust.com.marketplace.api.read.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import onlydust.com.marketplace.api.read.entities.project.ProjectPageV2ItemQueryEntity;

public interface ProjectsPageV2Repository extends JpaRepository<ProjectPageV2ItemQueryEntity, UUID> {

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
    Page<ProjectPageV2ItemQueryEntity> findAll(UUID[] projectIds,
                                               UUID[] categoryIds,
                                               UUID[] languageIds,
                                               UUID[] ecosystemIds,
                                               String[] tags,
                                               Pageable pageable);

    @Query(value = """
        SELECT  p.project_id                            as id,
                p.project_slug                          as slug,
                p.project_name                          as name,
                p.project ->> 'shortDescription'        as short_description,
                p.project ->> 'logoUrl'                 as logo_url,
                coalesce(pcd.contributor_count, 0)      as contributor_count,
                p.categories                            as categories,
                p.languages                             as languages,
                p.tags                                  as tags,
                coalesce(p.ecosystems, '[]')            as ecosystems,
                coalesce(pcd.good_first_issue_count, 0) as good_first_issue_count,
                coalesce(pcd.available_issue_count, 0)  as available_issue_count,
                coalesce(p.star_count, 0)               as star_count,
                coalesce(p.fork_count, 0)               as fork_count,
                coalesce(hic.issue_count, 0)            as od_hack_issue_count,
                coalesce(hic.available_issue_count, 0)  as od_hack_available_issue_count
            FROM bi.p_project_global_data p
                    JOIN bi.p_project_contributions_data pcd on p.project_id = pcd.project_id
                    JOIN hackathon_projects hp on p.project_id = hp.project_id
                    JOIN hackathons h on hp.hackathon_id = h.id
                    LEFT JOIN LATERAL (SELECT count(distinct gi.id)                                                            as issue_count,
                                            count(distinct gi.id) filter ( where gi.status = 'OPEN' and gia.user_id is null) as available_issue_count
                                        from hackathon_issues hi
                                                join indexer_exp.github_issues gi on hi.issue_id = gi.id
                                                left join indexer_exp.github_issues_assignees gia on gi.id = gia.issue_id
                                        where hi.hackathon_id = hp.hackathon_id
                                        and p.project_id = any (hi.project_ids)) hic on true
            WHERE h.slug = :hackathonSlug
            and (cast(:ecosystemIds as uuid[]) is null or p.ecosystem_ids && cast(:ecosystemIds as uuid[]))
            and (cast(:languageIds as uuid[]) is null or p.language_ids && cast(:languageIds as uuid[]))
            and (cast(:hasAvailableIssues as boolean) is null or (hic.available_issue_count > 0) = :hasAvailableIssues)
            and (cast(:search as text) is null or p.project_name ilike '%' || :search || '%')
        """, nativeQuery = true)
    Page<ProjectPageV2ItemQueryEntity> findHackathonProjects(String hackathonSlug,
                                                            UUID[] languageIds,
                                                            UUID[] ecosystemIds,
                                                            Boolean hasAvailableIssues,
                                                            String search,
                                                            Pageable pageable);
}

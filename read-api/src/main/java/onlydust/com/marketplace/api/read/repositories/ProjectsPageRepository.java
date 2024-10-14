package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectPageItemQueryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProjectsPageRepository extends JpaRepository<ProjectPageItemQueryEntity, UUID> {

    @Query(value = """
            SELECT p.project_id                                                                  as id,
                   p.project_slug                                                                as slug,
                   p.rank                                                                        as rank,
                   p.project_name                                                                as name,
                   p.project ->> 'shortDescription'                                              as short_description,
                   p.project ->> 'logoUrl'                                                       as logo_url,
                   p.project ->> 'hiring'                                                        as hiring,
                   p.project ->> 'visibility'                                                    as visibility,
                   coalesce(array_length(p.repo_ids, 1), 0)                                      as repo_count,
                   coalesce(cd.contributor_count, 0)                                             as contributor_count,
                   p.leads                                                                       as project_leads,
                   p.categories                                                                  as categories,
                   p.ecosystems                                                                  as ecosystems,
                   p.languages                                                                   as languages,
                   p.tags                                                                        as tags,
                   coalesce(cast(p.budget ->> 'availableBudgetUsd' as numeric), 0)
                                                                                                 as remaining_usd_budget,
                   coalesce(cd.good_first_issue_count, 0) > 0                                    as has_good_first_issues,
                   p.has_repos_without_github_app_installed                                      as has_repos_without_github_app_installed,
                   (:userId is not null and
                    p.invited_project_lead_ids is not null and :userId = any (p.invited_project_lead_ids) and
                    not (p.project_lead_ids is not null and :userId = any (p.project_lead_ids))) as is_invited_as_project_lead
            FROM bi.p_project_global_data p
                     LEFT JOIN (select cd.project_id,
                                       count(distinct cd.contributor_id)                                               as contributor_count,
                                       count(cd.contribution_id) filter ( where cd.is_good_first_issue and
                                                                                coalesce(array_length(cd.assignee_ids, 1), 0) = 0 and
                                                                                cd.contribution_status != 'COMPLETED' and
                                                                                cd.contribution_status != 'CANCELLED') as good_first_issue_count
                                from bi.p_contribution_data cd
                                group by cd.project_id) cd
                               on cd.project_id = p.project_id
            
            
            
            WHERE (p.project_visibility = 'PUBLIC' and array_length(p.repo_ids, 1) > 0 or
                   :userId = any (p.project_lead_ids) or
                   :userId = any (p.invited_project_lead_ids))
              and (:mine is null or :mine is false or
                   (:mine is true and :userId is not null and
                    (p.invited_project_lead_ids is not null and :userId = any (p.invited_project_lead_ids) or
                    p.project_lead_ids is not null and :userId = any (p.project_lead_ids))))
              and (cast(:ecosystemIds as uuid[]) is null or p.ecosystem_ids && cast(:ecosystemIds as uuid[]))
              and (cast(:ecosystemSlugs as text[]) is null or p.ecosystem_slugs && cast(:ecosystemSlugs as text[]))
              and (cast(:projectIds as uuid[]) is null or p.project_id = any (cast(:projectIds as uuid[])))
              and (cast(:projectSlugs as text[]) is null or p.project_slug = any (cast(:projectSlugs as text[])))
              and (cast(:categoryIds as uuid[]) is null or p.project_category_ids && cast(:categoryIds as uuid[]))
              and (cast(:categorySlugs as text[]) is null or p.project_category_slugs && cast(:categorySlugs as text[]))
              and (cast(:languageIds as uuid[]) is null or p.language_ids && cast(:languageIds as uuid[]))
              and (cast(:languageSlugs as text[]) is null or p.language_slugs && cast(:languageSlugs as text[]))
              and (cast(:tags as project_tag[]) is null or p.tags && cast(:tags as project_tag[]))
              and (cast(:hasGoodFirstIssues as boolean) is null or
                   cast(:hasGoodFirstIssues as boolean) is true and cd.good_first_issue_count > 0 or
                   cast(:hasGoodFirstIssues as boolean) is false and cd.good_first_issue_count = 0)
              and (cast(:search as text) is null or p.search ilike '%' || cast(:search as text) || '%')
            """, nativeQuery = true)
    Page<ProjectPageItemQueryEntity> findAll(UUID userId,
                                             Boolean mine,
                                             String search,
                                             UUID[] projectIds,
                                             String[] projectSlugs,
                                             UUID[] categoryIds,
                                             String[] categorySlugs,
                                             UUID[] languageIds,
                                             String[] languageSlugs,
                                             UUID[] ecosystemIds,
                                             String[] ecosystemSlugs,
                                             String[] tags,
                                             Boolean hasGoodFirstIssues,
                                             Pageable pageable);

}

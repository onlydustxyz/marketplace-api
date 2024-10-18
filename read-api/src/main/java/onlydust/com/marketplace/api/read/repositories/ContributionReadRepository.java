package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.contract.model.ContributionsQueryParams;
import onlydust.com.marketplace.api.contract.model.ContributionsSortEnum;
import onlydust.com.marketplace.api.contract.model.SortDirection;
import onlydust.com.marketplace.api.read.entities.bi.ContributionReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ContributionReadRepository extends Repository<ContributionReadEntity, Long> {

    @Query(value = """
            select c.contribution_uuid                       as contribution_uuid,
                   c.contribution_type                       as contribution_type,
                   c.github_repo                             as github_repo,
                   c.github_author                           as github_author,
                   c.github_number                           as github_number,
                   c.github_status                           as github_status,
                   c.github_title                            as github_title,
                   c.github_html_url                         as github_html_url,
                   c.github_body                             as github_body,
                   c.github_labels                           as github_labels,
                   c.updated_at                              as last_updated_at,
                   c.created_at                              as created_at,
                   c.completed_at                            as completed_at,
                   c.activity_status                         as activity_status,
                   c.project                                 as project,
                   c.contributors                            as contributors,
                   c.applicants                              as applicants,
                   c.languages                               as languages,
                   c.linked_issues                           as linked_issues,
                   coalesce(rd.total_rewarded_usd_amount, 0) as total_rewarded_usd_amount
            from bi.p_contribution_data c
                     left join bi.p_contribution_reward_data rd on rd.contribution_uuid = c.contribution_uuid
            where (coalesce(:ids) is null or c.contribution_uuid = any (:ids))
              and (coalesce(:types) is null or c.contribution_type = any (cast(:types as indexer_exp.contribution_type[])))
              and (coalesce(:projectIds) is null or c.project_id = any (:projectIds))
              and (coalesce(:projectSlugs) is null or c.project_slug = any (:projectSlugs))
              and (coalesce(:statuses) is null or c.activity_status = any (:statuses))
              and (coalesce(:repoIds) is null or c.repo_id = any (:repoIds))
              and (coalesce(:contributorIds) is null or c.contributor_ids && :contributorIds)
              and (coalesce(:hasBeenRewarded) is null or :hasBeenRewarded = (coalesce(rd.total_rewarded_usd_amount, 0) > 0))
            """, nativeQuery = true)
    Page<ContributionReadEntity> findAll(UUID[] ids,
                                         String[] types,
                                         UUID[] projectIds,
                                         String[] projectSlugs,
                                         String[] statuses,
                                         Long[] repoIds,
                                         Long[] contributorIds,
                                         Boolean hasBeenRewarded,
                                         Pageable pageable);

    default Page<ContributionReadEntity> findAll(ContributionsQueryParams q) {
        return findAll(
                q.getIds() == null ? null : q.getIds().toArray(UUID[]::new),
                q.getTypes() == null ? null : q.getTypes().stream().map(Enum::name).toArray(String[]::new),
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                q.getProjectSlugs() == null ? null : q.getProjectSlugs().toArray(String[]::new),
                q.getStatuses() == null ? null : q.getStatuses().stream().map(Enum::name).toArray(String[]::new),
                q.getRepoIds() == null ? null : q.getRepoIds().toArray(Long[]::new),
                q.getContributorIds() == null ? null : q.getContributorIds().toArray(Long[]::new),
                q.getHasBeenRewarded(),
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC :
                        Sort.Direction.ASC, getSortProperty(q.getSort()))));
    }

    static String getSortProperty(ContributionsSortEnum sort) {
        return sort == null ? "created_at" : switch (sort) {
            case CREATED_AT -> "created_at";
            case TYPE -> "contribution_type";
        };
    }
}

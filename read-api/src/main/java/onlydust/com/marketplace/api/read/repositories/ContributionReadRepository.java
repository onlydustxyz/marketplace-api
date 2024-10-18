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
                   ccd.github_author                         as github_author,
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
                   ccd.contributors                          as contributors,
                   ccd.applicants                            as applicants,
                   c.languages                               as languages,
                   c.linked_issues                           as linked_issues,
                   coalesce(rd.total_rewarded_usd_amount, 0) as total_rewarded_usd_amount,
                   rd.reward_ids                             as reward_ids
            from bi.p_contribution_data c
                     join bi.p_contribution_contributors_data ccd on c.contribution_uuid = ccd.contribution_uuid
                     left join bi.p_contribution_reward_data rd on rd.contribution_uuid = c.contribution_uuid
                     left join project_contributor_labels pcl on coalesce(:projectContributorLabelIds) is not null and pcl.project_id = c.project_id
                     left join contributor_project_contributor_labels cpcl on coalesce(:projectContributorLabelIds) is not null and cpcl.label_id = pcl.id and cpcl.github_user_id = any (ccd.contributor_ids)
            where (coalesce(:ids) is null or c.contribution_uuid = any (:ids))
              and (coalesce(:types) is null or c.contribution_type = any (cast(:types as indexer_exp.contribution_type[])))
              and (coalesce(:projectIds) is null or c.project_id = any (:projectIds))
              and (coalesce(:projectSlugs) is null or c.project_slug = any (:projectSlugs))
              and (coalesce(:statuses) is null or c.activity_status = any (cast(:statuses as activity_status[])))
              and (coalesce(:repoIds) is null or c.repo_id = any (:repoIds))
              and (coalesce(:contributorIds) is null or ccd.contributor_ids && :contributorIds)
              and (coalesce(:projectContributorLabelIds) is null or cpcl.label_id = any (:projectContributorLabelIds))
              and (coalesce(:rewardIds) is null or rd.reward_ids && :rewardIds)
              and (coalesce(:hasBeenRewarded) is null or :hasBeenRewarded = (coalesce(rd.total_rewarded_usd_amount, 0) > 0))
              and (coalesce(:search) is null or c.search ilike '%' || :search || '%' or ccd.search ilike '%' || :search || '%')
            group by c.contribution_uuid,
                     ccd.contribution_uuid,
                     rd.contribution_uuid
            """, nativeQuery = true)
    Page<ContributionReadEntity> findAll(UUID[] ids,
                                         String[] types,
                                         UUID[] projectIds,
                                         String[] projectSlugs,
                                         String[] statuses,
                                         Long[] repoIds,
                                         Long[] contributorIds,
                                         UUID[] projectContributorLabelIds,
                                         UUID[] rewardIds,
                                         Boolean hasBeenRewarded,
                                         String search,
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
                q.getProjectContributorLabelIds() == null ? null : q.getProjectContributorLabelIds().toArray(UUID[]::new),
                q.getRewardIds() == null ? null : q.getRewardIds().toArray(UUID[]::new),
                q.getHasBeenRewarded(),
                q.getSearch(),
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

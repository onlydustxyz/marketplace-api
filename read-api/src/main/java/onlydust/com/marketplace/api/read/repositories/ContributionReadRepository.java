package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.ContributionsQueryParams;
import onlydust.com.marketplace.api.contract.model.ContributionsSortEnum;
import onlydust.com.marketplace.api.contract.model.DataSourceEnum;
import onlydust.com.marketplace.api.contract.model.SortDirection;
import onlydust.com.marketplace.api.read.entities.bi.ContributionReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.parseZonedNullable;

public interface ContributionReadRepository extends Repository<ContributionReadEntity, Long> {

    @Query(value = """
            select c.contribution_uuid                       as contribution_uuid,
                   c.contribution_type                       as contribution_type,
                   c.project_id                              as project_id,
                   c.github_id                               as github_id,
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
                   ccd.github_merged_by                      as merged_by,
                   c.languages                               as languages,
                   c.linked_issues                           as linked_issues,
                   c.github_comment_count                    as github_comment_count,
                   coalesce(rd.total_rewarded_usd_amount, 0) as total_rewarded_usd_amount,
                   rd.per_recipient                          as rewarded_per_recipients
            from bi.p_contribution_data c
                     join bi.p_contribution_contributors_data ccd on c.contribution_uuid = ccd.contribution_uuid
                     left join bi.p_contribution_reward_data rd on rd.contribution_uuid = c.contribution_uuid
                     left join project_contributor_labels pcl on coalesce(:projectContributorLabelIds) is not null and pcl.project_id = c.project_id
                     left join contributor_project_contributor_labels cpcl on coalesce(:projectContributorLabelIds) is not null and cpcl.label_id = pcl.id and cpcl.github_user_id = any (ccd.contributor_ids)
                     left join indexer_exp.github_pull_requests_closing_issues ci on ci.issue_id = c.issue_id
            where (coalesce(:fromDate) is null or c.timestamp >= :fromDate)
              and (coalesce(:toDate) is null or c.timestamp < :toDate)
              and (:onlyOnlyDustData is false or c.project_id is not null)
              and (coalesce(:ids) is null or c.contribution_uuid = any (:ids))
              and (coalesce(:types) is null or c.contribution_type = any (cast(:types as indexer_exp.contribution_type[])))
              and (coalesce(:projectIds) is null or c.project_id = any (:projectIds))
              and (coalesce(:projectSlugs) is null or c.project_slug = any (:projectSlugs))
              and (coalesce(:statuses) is null or c.activity_status = any (cast(:statuses as activity_status[])))
              and (coalesce(:repoIds) is null or c.repo_id = any (:repoIds))
              and (coalesce(:contributorIds) is null or ccd.contributor_ids && :contributorIds)
              and (coalesce(:applicantIds) is null or ccd.applicant_ids && :applicantIds)
              and (coalesce(:projectContributorLabelIds) is null or cpcl.label_id = any (:projectContributorLabelIds))
              and (coalesce(:rewardIds) is null or rd.reward_ids && :rewardIds)
              and (coalesce(:languageIds) is null or c.language_ids && :languageIds)
              and (coalesce(:hasBeenRewarded) is null or :hasBeenRewarded = (coalesce(rd.total_rewarded_usd_amount, 0) > 0))
              and (coalesce(:search) is null or c.search ilike '%' || :search || '%' or ccd.search ilike '%' || :search || '%')
              and (coalesce(:showLinkedIssues) is null or :showLinkedIssues = (ci.pull_request_id is not null))
            group by c.contribution_uuid,
                     ccd.contribution_uuid,
                     rd.contribution_uuid
            """, nativeQuery = true)
    Page<ContributionReadEntity> findAll(ZonedDateTime fromDate,
                                         ZonedDateTime toDate,
                                         @NonNull Boolean onlyOnlyDustData,
                                         UUID[] ids,
                                         String[] types,
                                         UUID[] projectIds,
                                         String[] projectSlugs,
                                         String[] statuses,
                                         Long[] repoIds,
                                         Long[] contributorIds,
                                         Long[] applicantIds,
                                         UUID[] projectContributorLabelIds,
                                         UUID[] rewardIds,
                                         UUID[] languageIds,
                                         Boolean hasBeenRewarded,
                                         Boolean showLinkedIssues,
                                         String search,
                                         Pageable pageable);

    default Page<ContributionReadEntity> findAll(ContributionsQueryParams q) {
        final var sanitizedFromDate = q.getFromDate() == null ? null : parseZonedNullable(q.getFromDate()).truncatedTo(ChronoUnit.DAYS);
        final var sanitizedToDate = q.getToDate() == null ? null : parseZonedNullable(q.getToDate()).truncatedTo(ChronoUnit.DAYS).plusDays(1);

        return findAll(sanitizedFromDate,
                sanitizedToDate,
                q.getDataSource() == DataSourceEnum.ONLYDUST,
                q.getIds() == null ? null : q.getIds().toArray(UUID[]::new),
                q.getTypes() == null ? null : q.getTypes().stream().map(Enum::name).toArray(String[]::new),
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                q.getProjectSlugs() == null ? null : q.getProjectSlugs().toArray(String[]::new),
                q.getStatuses() == null ? null : q.getStatuses().stream().map(Enum::name).toArray(String[]::new),
                q.getRepoIds() == null ? null : q.getRepoIds().toArray(Long[]::new),
                q.getContributorIds() == null ? null : q.getContributorIds().toArray(Long[]::new),
                q.getApplicantIds() == null ? null : q.getApplicantIds().toArray(Long[]::new),
                q.getProjectContributorLabelIds() == null ? null : q.getProjectContributorLabelIds().toArray(UUID[]::new),
                q.getRewardIds() == null ? null : q.getRewardIds().toArray(UUID[]::new),
                q.getLanguageIds() == null ? null : q.getLanguageIds().toArray(UUID[]::new),
                q.getHasBeenRewarded(),
                q.getShowLinkedIssues(),
                q.getSearch(),
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC :
                        Sort.Direction.ASC, getSortProperty(q.getSort()))));
    }

    static String getSortProperty(ContributionsSortEnum sort) {
        return sort == null ? "created_at" : switch (sort) {
            case CREATED_AT -> "created_at";
            case UPDATED_AT -> "updated_at";
            case TYPE -> "contribution_type";
        };
    }
}

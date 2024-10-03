package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.ContributorKpiSortEnum;
import onlydust.com.marketplace.api.read.entities.bi.ContributorKpisReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public interface ContributorKpisReadRepository extends Repository<ContributorKpisReadEntity, Long> {

    static String getSortProperty(ContributorKpiSortEnum sort) {
        return sort == null ? "contributor_login" : switch (sort) {
            case CONTRIBUTOR_LOGIN -> "contributor_login";
            case PROJECT_NAME -> "first_project_name";
            case TOTAL_REWARDED_USD_AMOUNT -> "total_rewarded_usd_amount";
            case ISSUE_COUNT -> "issue_count";
            case PR_COUNT -> "pr_count";
            case CODE_REVIEW_COUNT -> "code_review_count";
            case CONTRIBUTION_COUNT -> "contribution_count";
        };
    }

    @Query(value = """
            SELECT -- /// global data /// --
                   d.contributor_id,
                   d.contributor_login,
                   d.contributor_country,
                   d.contributor,
                   d.projects,
                   d.categories,
                   d.languages,
                   d.ecosystems,
                   contributor_labels.list as project_contributor_labels,
                   -- /// filtered & computed data /// --
                   coalesce(d.total_rewarded_usd_amount, 0)                 as total_rewarded_usd_amount,
                   coalesce(d.reward_count, 0)                              as reward_count,
                   coalesce(d.contribution_count, 0)                        as contribution_count,
                   coalesce(d.issue_count, 0)                               as issue_count,
                   coalesce(d.pr_count, 0)                                  as pr_count,
                   coalesce(d.code_review_count, 0)                         as code_review_count,
                   coalesce(previous_period.total_rewarded_usd_amount, 0)   as previous_period_total_rewarded_usd_amount,
                   coalesce(previous_period.reward_count, 0)                as previous_period_reward_count,
                   coalesce(previous_period.contribution_count, 0)          as previous_period_contribution_count,
                   coalesce(previous_period.issue_count, 0)                 as previous_period_issue_count,
                   coalesce(previous_period.pr_count, 0)                    as previous_period_pr_count,
                   coalesce(previous_period.code_review_count, 0)           as previous_period_code_review_count
            
            FROM bi.select_contributors(:fromDate, :toDate, :dataSourceIds, :contributorIds, :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, cast(:contributionStatuses as indexer_exp.contribution_status[]), :search, :showFilteredKpis) d
                     LEFT JOIN (
                            select * from bi.select_contributors(:fromDatePreviousPeriod, :toDatePreviousPeriod, :dataSourceIds, :contributorIds, :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, cast(:contributionStatuses as indexer_exp.contribution_status[]), :search, :showFilteredKpis)
                         ) previous_period ON previous_period.contributor_id = d.contributor_id
            
                     LEFT JOIN LATERAL ( select jsonb_agg(jsonb_build_object('id', pcl.id, 'slug', pcl.slug, 'name', pcl.name)) as list
                                         from contributor_project_contributor_labels cpcl
                                            join project_contributor_labels pcl on pcl.id = cpcl.label_id
                                            join projects p on p.id = pcl.project_id
                                         where cpcl.github_user_id = d.contributor_id and
                                               (coalesce(:projectIds) is null or p.id = any(:projectIds)) and
                                               (coalesce(:projectSlugs) is null or p.slug = any(:projectSlugs))) contributor_labels ON true
            
            WHERE (coalesce(:totalRewardedUsdAmountMin) is null or d.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
              and (coalesce(:totalRewardedUsdAmountEq) is null or d.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
              and (coalesce(:totalRewardedUsdAmountMax) is null or d.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
              and (coalesce(:rewardCountMin) is null or d.reward_count >= :rewardCountMin)
              and (coalesce(:rewardCountEq) is null or d.reward_count = :rewardCountEq)
              and (coalesce(:rewardCountMax) is null or d.reward_count <= :rewardCountMax)
              and (coalesce(:contributionCountMin) is null or (
                  case when 'ISSUE' = any(:contributionTypes) then d.issue_count else 0 end +
                  case when 'PULL_REQUEST' = any(:contributionTypes) then d.pr_count else 0 end +
                  case when 'CODE_REVIEW' = any(:contributionTypes) then d.code_review_count else 0 end
              ) >= :contributionCountMin)
              and (coalesce(:contributionCountEq) is null or (
                  case when 'ISSUE' = any(:contributionTypes) then d.issue_count else 0 end +
                  case when 'PULL_REQUEST' = any(:contributionTypes) then d.pr_count else 0 end +
                  case when 'CODE_REVIEW' = any(:contributionTypes) then d.code_review_count else 0 end
              ) = :contributionCountEq)
              and (coalesce(:contributionCountMax) is null or (
                  case when 'ISSUE' = any(:contributionTypes) then d.issue_count else 0 end +
                  case when 'PULL_REQUEST' = any(:contributionTypes) then d.pr_count else 0 end +
                  case when 'CODE_REVIEW' = any(:contributionTypes) then d.code_review_count else 0 end
              ) <= :contributionCountMax)
            """,
            countQuery = """
                    SELECT count(d.contributor_id)
                    FROM bi.select_contributors(:fromDate, :toDate, :dataSourceIds, :contributorIds, :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, cast(:contributionStatuses as indexer_exp.contribution_status[]), :search, :showFilteredKpis) d
                    WHERE (coalesce(:totalRewardedUsdAmountMin) is null or d.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
                      and (coalesce(:totalRewardedUsdAmountEq) is null or d.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
                      and (coalesce(:totalRewardedUsdAmountMax) is null or d.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
                      and (coalesce(:rewardCountMin) is null or d.reward_count >= :rewardCountMin)
                      and (coalesce(:rewardCountEq) is null or d.reward_count = :rewardCountEq)
                      and (coalesce(:rewardCountMax) is null or d.reward_count <= :rewardCountMax)
                      and (coalesce(:contributionCountMin) is null or (
                          case when 'ISSUE' = any(:contributionTypes) then d.issue_count else 0 end +
                          case when 'PULL_REQUEST' = any(:contributionTypes) then d.pr_count else 0 end +
                          case when 'CODE_REVIEW' = any(:contributionTypes) then d.code_review_count else 0 end
                      ) >= :contributionCountMin)
                      and (coalesce(:contributionCountEq) is null or (
                          case when 'ISSUE' = any(:contributionTypes) then d.issue_count else 0 end +
                          case when 'PULL_REQUEST' = any(:contributionTypes) then d.pr_count else 0 end +
                          case when 'CODE_REVIEW' = any(:contributionTypes) then d.code_review_count else 0 end
                      ) = :contributionCountEq)
                      and (coalesce(:contributionCountMax) is null or (
                          case when 'ISSUE' = any(:contributionTypes) then d.issue_count else 0 end +
                          case when 'PULL_REQUEST' = any(:contributionTypes) then d.pr_count else 0 end +
                          case when 'CODE_REVIEW' = any(:contributionTypes) then d.code_review_count else 0 end
                      ) <= :contributionCountMax)
                    """,
            nativeQuery = true)
    Page<ContributorKpisReadEntity> findAll(@NonNull ZonedDateTime fromDate,
                                            @NonNull ZonedDateTime toDate,
                                            @NonNull ZonedDateTime fromDatePreviousPeriod,
                                            @NonNull ZonedDateTime toDatePreviousPeriod,
                                            @NonNull UUID[] dataSourceIds,
                                            @NonNull Boolean showFilteredKpis,
                                            String search,
                                            Long[] contributorIds,
                                            UUID[] projectIds,
                                            String[] projectSlugs,
                                            UUID[] categoryIds,
                                            UUID[] languageIds,
                                            UUID[] ecosystemIds,
                                            String[] countryCodes,
                                            String[] contributionStatuses,
                                            BigDecimal totalRewardedUsdAmountMin,
                                            BigDecimal totalRewardedUsdAmountEq,
                                            BigDecimal totalRewardedUsdAmountMax,
                                            Integer rewardCountMin,
                                            Integer rewardCountEq,
                                            Integer rewardCountMax,
                                            Integer contributionCountMin,
                                            Integer contributionCountEq,
                                            Integer contributionCountMax,
                                            String[] contributionTypes,
                                            Pageable pageable);
}

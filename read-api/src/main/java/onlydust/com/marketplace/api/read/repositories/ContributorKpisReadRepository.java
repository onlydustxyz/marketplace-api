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
            
            FROM bi.select_contributors(:fromDate, :toDate, :programOrEcosystemIds, :contributorIds, :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, cast(:contributionStatuses as indexer_exp.contribution_status[]), :search) d
                     LEFT JOIN (
                            select * from bi.select_contributors(:fromDatePreviousPeriod, :toDatePreviousPeriod, :programOrEcosystemIds, :contributorIds, :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, cast(:contributionStatuses as indexer_exp.contribution_status[]), :search)
                         ) previous_period ON previous_period.contributor_id = d.contributor_id
            
            WHERE (coalesce(:totalRewardedUsdAmountMin) is null or d.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
              and (coalesce(:totalRewardedUsdAmountEq) is null or d.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
              and (coalesce(:totalRewardedUsdAmountMax) is null or d.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
              and (coalesce(:rewardCountMin) is null or d.reward_count >= :rewardCountMin)
              and (coalesce(:rewardCountEq) is null or d.reward_count = :rewardCountEq)
              and (coalesce(:rewardCountMax) is null or d.reward_count <= :rewardCountMax)
              and (coalesce(:contributionCountMin) is null or d.contribution_count >= :contributionCountMin)
              and (coalesce(:contributionCountEq) is null or d.contribution_count = :contributionCountEq)
              and (coalesce(:contributionCountMax) is null or d.contribution_count <= :contributionCountMax)
              and (coalesce(:issueCountMin) is null or d.issue_count >= :issueCountMin)
              and (coalesce(:issueCountEq) is null or d.issue_count = :issueCountEq)
              and (coalesce(:issueCountMax) is null or d.issue_count <= :issueCountMax)
              and (coalesce(:prCountMin) is null or d.pr_count >= :prCountMin)
              and (coalesce(:prCountEq) is null or d.pr_count = :prCountEq)
              and (coalesce(:prCountMax) is null or d.pr_count <= :prCountMax)
              and (coalesce(:codeReviewCountMin) is null or d.code_review_count >= :codeReviewCountMin)
              and (coalesce(:codeReviewCountEq) is null or d.code_review_count = :codeReviewCountEq)
              and (coalesce(:codeReviewCountMax) is null or d.code_review_count <= :codeReviewCountMax)
            """,
            countQuery = """
                    SELECT count(d.contributor_id)
                    FROM bi.select_contributors(:fromDate, :toDate, :programOrEcosystemIds, :contributorIds, :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, cast(:contributionStatuses as indexer_exp.contribution_status[]), :search) d
                    WHERE (coalesce(:totalRewardedUsdAmountMin) is null or d.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
                      and (coalesce(:totalRewardedUsdAmountEq) is null or d.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
                      and (coalesce(:totalRewardedUsdAmountMax) is null or d.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
                      and (coalesce(:rewardCountMin) is null or d.reward_count >= :rewardCountMin)
                      and (coalesce(:rewardCountEq) is null or d.reward_count = :rewardCountEq)
                      and (coalesce(:rewardCountMax) is null or d.reward_count <= :rewardCountMax)
                      and (coalesce(:contributionCountMin) is null or d.contribution_count >= :contributionCountMin)
                      and (coalesce(:contributionCountEq) is null or d.contribution_count = :contributionCountEq)
                      and (coalesce(:contributionCountMax) is null or d.contribution_count <= :contributionCountMax)
                      and (coalesce(:issueCountMin) is null or d.issue_count >= :issueCountMin)
                      and (coalesce(:issueCountEq) is null or d.issue_count = :issueCountEq)
                      and (coalesce(:issueCountMax) is null or d.issue_count <= :issueCountMax)
                      and (coalesce(:prCountMin) is null or d.pr_count >= :prCountMin)
                      and (coalesce(:prCountEq) is null or d.pr_count = :prCountEq)
                      and (coalesce(:prCountMax) is null or d.pr_count <= :prCountMax)
                      and (coalesce(:codeReviewCountMin) is null or d.code_review_count >= :codeReviewCountMin)
                      and (coalesce(:codeReviewCountEq) is null or d.code_review_count = :codeReviewCountEq)
                      and (coalesce(:codeReviewCountMax) is null or d.code_review_count <= :codeReviewCountMax)
                    """,
            nativeQuery = true)
    Page<ContributorKpisReadEntity> findAll(@NonNull ZonedDateTime fromDate,
                                            @NonNull ZonedDateTime toDate,
                                            @NonNull ZonedDateTime fromDatePreviousPeriod,
                                            @NonNull ZonedDateTime toDatePreviousPeriod,
                                            @NonNull UUID[] programOrEcosystemIds,
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
                                            Integer issueCountMin,
                                            Integer issueCountEq,
                                            Integer issueCountMax,
                                            Integer prCountMin,
                                            Integer prCountEq,
                                            Integer prCountMax,
                                            Integer codeReviewCountMin,
                                            Integer codeReviewCountEq,
                                            Integer codeReviewCountMax,
                                            Integer contributionCountMin,
                                            Integer contributionCountEq,
                                            Integer contributionCountMax,
                                            Pageable pageable);
}

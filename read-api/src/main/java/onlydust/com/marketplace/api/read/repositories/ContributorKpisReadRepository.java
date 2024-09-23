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
            case MERGED_PR_COUNT -> "merged_pr_count";
            case CONTRIBUTION_COUNT -> "contribution_count";
        };
    }

    @Query(value = """
            SELECT -- /// global data /// --
                   d.contributor_id,
                   d.contributor_login,
                   d.contributor,
                   d.projects,
                   d.categories,
                   d.languages,
                   d.ecosystems,
                   d.country_code,
                   -- /// filtered & computed data /// --
                   coalesce(d.total_rewarded_usd_amount, 0)                 as total_rewarded_usd_amount,
                   coalesce(d.merged_pr_count, 0)                           as merged_pr_count,
                   coalesce(d.reward_count, 0)                              as reward_count,
                   coalesce(d.contribution_count, 0)                        as contribution_count,
                   coalesce(previous_period.total_rewarded_usd_amount, 0)   as previous_period_total_rewarded_usd_amount,
                   coalesce(previous_period.merged_pr_count, 0)             as previous_period_merged_pr_count,
                   coalesce(previous_period.reward_count, 0)                as previous_period_reward_count,
                   coalesce(previous_period.contribution_count, 0)          as previous_period_contribution_count
            
            FROM bi.select_contributors(:fromDate, :toDate, :programOrEcosystemIds, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, :contributorRoles, :search) d
                     LEFT JOIN (
                            select * from bi.select_contributors(:fromDatePreviousPeriod, :toDatePreviousPeriod, :programOrEcosystemIds, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, :contributorRoles, :search) 
                         ) previous_period ON previous_period.contributor_id = d.contributor_id
            
            WHERE (coalesce(:totalRewardedUsdAmountMin) is null or d.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
              and (coalesce(:totalRewardedUsdAmountEq) is null or d.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
              and (coalesce(:totalRewardedUsdAmountMax) is null or d.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
              and (coalesce(:mergedPrCountMin) is null or d.merged_pr_count >= :mergedPrCountMin)
              and (coalesce(:mergedPrCountEq) is null or d.merged_pr_count = :mergedPrCountEq)
              and (coalesce(:mergedPrCountMax) is null or d.merged_pr_count <= :mergedPrCountMax)
              and (coalesce(:rewardCountMin) is null or d.reward_count >= :rewardCountMin)
              and (coalesce(:rewardCountEq) is null or d.reward_count = :rewardCountEq)
              and (coalesce(:rewardCountMax) is null or d.reward_count <= :rewardCountMax)
              and (coalesce(:contributionCountMin) is null or d.contribution_count >= :contributionCountMin)
              and (coalesce(:contributionCountEq) is null or d.contribution_count = :contributionCountEq)
              and (coalesce(:contributionCountMax) is null or d.contribution_count <= :contributionCountMax)
            ORDER BY ?#{#pageable}
            """,
            countQuery = """
                    SELECT count(d.contributor_id)
                    FROM bi.select_contributors(:fromDate, :toDate, :programOrEcosystemIds, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, :contributorRoles, :search) d
                    WHERE (coalesce(:totalRewardedUsdAmountMin) is null or d.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
                      and (coalesce(:totalRewardedUsdAmountEq) is null or d.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
                      and (coalesce(:totalRewardedUsdAmountMax) is null or d.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
                      and (coalesce(:mergedPrCountMin) is null or d.merged_pr_count >= :mergedPrCountMin)
                      and (coalesce(:mergedPrCountEq) is null or d.merged_pr_count = :mergedPrCountEq)
                      and (coalesce(:mergedPrCountMax) is null or d.merged_pr_count <= :mergedPrCountMax)
                      and (coalesce(:rewardCountMin) is null or d.reward_count >= :rewardCountMin)
                      and (coalesce(:rewardCountEq) is null or d.reward_count = :rewardCountEq)
                      and (coalesce(:rewardCountMax) is null or d.reward_count <= :rewardCountMax)
                      and (coalesce(:contributionCountMin) is null or d.contribution_count >= :contributionCountMin)
                      and (coalesce(:contributionCountEq) is null or d.contribution_count = :contributionCountEq)
                      and (coalesce(:contributionCountMax) is null or d.contribution_count <= :contributionCountMax)
                    ORDER BY ?#{#pageable}
                    """,
            nativeQuery = true)
    Page<ContributorKpisReadEntity> findAll(@NonNull ZonedDateTime fromDate,
                                            @NonNull ZonedDateTime toDate,
                                            @NonNull ZonedDateTime fromDatePreviousPeriod,
                                            @NonNull ZonedDateTime toDatePreviousPeriod,
                                            @NonNull UUID[] programOrEcosystemIds,
                                            String search,
                                            UUID[] categoryIds,
                                            UUID[] languageIds,
                                            UUID[] ecosystemIds,
                                            String[] countryCodes,
                                            String[] contributorRoles,
                                            BigDecimal totalRewardedUsdAmountMin,
                                            BigDecimal totalRewardedUsdAmountEq,
                                            BigDecimal totalRewardedUsdAmountMax,
                                            Integer mergedPrCountMin,
                                            Integer mergedPrCountEq,
                                            Integer mergedPrCountMax,
                                            Integer rewardCountMin,
                                            Integer rewardCountEq,
                                            Integer rewardCountMax,
                                            Integer contributionCountMin,
                                            Integer contributionCountEq,
                                            Integer contributionCountMax,
                                            Pageable pageable);
}

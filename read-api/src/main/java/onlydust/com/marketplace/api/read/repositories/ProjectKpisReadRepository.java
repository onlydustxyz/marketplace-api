package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.ProjectKpiSortEnum;
import onlydust.com.marketplace.api.read.entities.bi.ProjectKpisReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public interface ProjectKpisReadRepository extends Repository<ProjectKpisReadEntity, UUID> {

    @Query(value = """
            SELECT d.project_id,
                   coalesce(d.available_budget, 0)                          as available_budget,
                   coalesce(d.percent_spent_budget, 0)                      as percent_spent_budget,
                   coalesce(d.total_granted_usd_amount, 0)                  as total_granted_usd_amount,
                   coalesce(d.contribution_count, 0)                        as contribution_count,
                   coalesce(d.reward_count, 0)                              as reward_count,
                   coalesce(d.total_rewarded_usd_amount, 0)                 as total_rewarded_usd_amount,
                   coalesce(d.average_reward_usd_amount, 0)                 as average_reward_usd_amount,
                   coalesce(d.merged_pr_count, 0)                           as merged_pr_count,
                   coalesce(d.active_contributor_count, 0)                  as active_contributor_count,
                   coalesce(d.onboarded_contributor_count, 0)               as onboarded_contributor_count,
                   pgd.project_name,
                   pgd.project,
                   pgd.leads,
                   pgd.categories,
                   pgd.languages,
                   pgd.ecosystems,
                   pgd.programs,
                   coalesce(previous_period.available_budget, 0)            as previous_period_available_budget,
                   coalesce(previous_period.percent_spent_budget, 0)        as previous_period_percent_spent_budget,
                   coalesce(previous_period.total_granted_usd_amount, 0)    as previous_period_total_granted_usd_amount,
                   coalesce(previous_period.contribution_count, 0)          as previous_period_contribution_count,
                   coalesce(previous_period.reward_count, 0)                as previous_period_reward_count,
                   coalesce(previous_period.total_rewarded_usd_amount, 0)   as previous_period_total_rewarded_usd_amount,
                   coalesce(previous_period.average_reward_usd_amount, 0)   as previous_period_average_reward_usd_amount,
                   coalesce(previous_period.merged_pr_count, 0)             as previous_period_merged_pr_count,
                   coalesce(previous_period.active_contributor_count, 0)    as previous_period_active_contributor_count,
                   coalesce(previous_period.onboarded_contributor_count, 0) as previous_period_onboarded_contributor_count
            FROM (SELECT d.project_id                                                                          as project_id,
                         sum(d.granted_usd_amount) - sum(d.rewarded_usd_amount)                                as available_budget,
                         sum(d.granted_usd_amount) / greatest(sum(d.rewarded_usd_amount), 1)                   as percent_spent_budget,
                         sum(d.granted_usd_amount)                                                             as total_granted_usd_amount,
                         count(d.contribution_id)                                                              as contribution_count,
                         count(d.reward_id)                                                                    as reward_count,
                         sum(d.rewarded_usd_amount)                                                            as total_rewarded_usd_amount,
                         avg(d.rewarded_usd_amount)                                                            as average_reward_usd_amount,
                         count(d.contribution_id) filter ( where d.is_merged_pr is true )                      as merged_pr_count,
                         count(distinct d.contributor_id)                                                      as active_contributor_count,
                         count(distinct d.contributor_id) filter ( where d.is_first_contribution_on_onlydust ) as onboarded_contributor_count
                  from bi.project_data_unions d
                  where d.timestamp >= :fromDate
                    and d.timestamp < :toDate
                    and (coalesce(:projectLeadIds) is null or d.project_lead_ids && cast(:projectLeadIds as uuid[]))
                    and (coalesce(:categoryIds) is null or d.project_category_ids && cast(:categoryIds as uuid[]))
                    and (coalesce(:languageIds) is null or d.language_ids && cast(:languageIds as uuid[]))
                    and (coalesce(:ecosystemIds) is null or d.ecosystem_ids && cast(:ecosystemIds as uuid[]))
                  group by d.project_id) d
            
                     JOIN bi.project_global_data pgd on pgd.project_id = d.project_id
            
                     LEFT JOIN LATERAL (SELECT previous_period.project_id                                                                                        as project_id,
                                               sum(previous_period.granted_usd_amount) - sum(previous_period.rewarded_usd_amount)                                as available_budget,
                                               sum(previous_period.granted_usd_amount) / greatest(sum(previous_period.rewarded_usd_amount), 1)                   as percent_spent_budget,
                                               sum(previous_period.granted_usd_amount)                                                                           as total_granted_usd_amount,
                                               count(previous_period.contribution_id)                                                                            as contribution_count,
                                               count(previous_period.reward_id)                                                                                  as reward_count,
                                               sum(previous_period.rewarded_usd_amount)                                                                          as total_rewarded_usd_amount,
                                               avg(previous_period.rewarded_usd_amount)                                                                          as average_reward_usd_amount,
                                               count(previous_period.contribution_id) filter ( where previous_period.is_merged_pr is true )                      as merged_pr_count,
                                               count(distinct previous_period.contributor_id)                                                                    as active_contributor_count,
                                               count(distinct previous_period.contributor_id) filter ( where previous_period.is_first_contribution_on_onlydust ) as onboarded_contributor_count
                                        from bi.project_data_unions previous_period
                                        where previous_period.project_id = d.project_id
                                          and previous_period.timestamp >= :fromDatePreviousPeriod
                                          and previous_period.timestamp < :toDatePreviousPeriod
                                          and (coalesce(:projectLeadIds) is null or previous_period.project_lead_ids && cast(:projectLeadIds as uuid[]))
                                          and (coalesce(:categoryIds) is null or previous_period.project_category_ids && cast(:categoryIds as uuid[]))
                                          and (coalesce(:languageIds) is null or previous_period.language_ids && cast(:languageIds as uuid[]))
                                          and (coalesce(:ecosystemIds) is null or previous_period.ecosystem_ids && cast(:ecosystemIds as uuid[]))
                                        group by previous_period.project_id ) previous_period ON true
            
            WHERE (coalesce(:availableBudgetUsdAmountMin) is null or d.available_budget >= :availableBudgetUsdAmountMin)
              and (coalesce(:availableBudgetUsdAmountEq) is null or d.available_budget = :availableBudgetUsdAmountEq)
              and (coalesce(:availableBudgetUsdAmountMax) is null or d.available_budget <= :availableBudgetUsdAmountMax)
              and (coalesce(:percentUsedBudgetMin) is null or d.percent_spent_budget >= :percentUsedBudgetMin)
              and (coalesce(:percentUsedBudgetEq) is null or d.percent_spent_budget = :percentUsedBudgetEq)
              and (coalesce(:percentUsedBudgetMax) is null or d.percent_spent_budget <= :percentUsedBudgetMax)
              and (coalesce(:totalGrantedUsdAmountMin) is null or d.total_granted_usd_amount >= :totalGrantedUsdAmountMin)
              and (coalesce(:totalGrantedUsdAmountEq) is null or d.total_granted_usd_amount = :totalGrantedUsdAmountEq)
              and (coalesce(:totalGrantedUsdAmountMax) is null or d.total_granted_usd_amount <= :totalGrantedUsdAmountMax)
              and (coalesce(:averageRewardUsdAmountMin) is null or d.average_reward_usd_amount >= :averageRewardUsdAmountMin)
              and (coalesce(:averageRewardUsdAmountEq) is null or d.average_reward_usd_amount = :averageRewardUsdAmountEq)
              and (coalesce(:averageRewardUsdAmountMax) is null or d.average_reward_usd_amount <= :averageRewardUsdAmountMax)
              and (coalesce(:totalRewardedUsdAmountMin) is null or d.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
              and (coalesce(:totalRewardedUsdAmountEq) is null or d.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
              and (coalesce(:totalRewardedUsdAmountMax) is null or d.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
              and (coalesce(:onboardedContributorCountMin) is null or d.onboarded_contributor_count >= :onboardedContributorCountMin)
              and (coalesce(:onboardedContributorCountEq) is null or d.onboarded_contributor_count = :onboardedContributorCountEq)
              and (coalesce(:onboardedContributorCountMax) is null or d.onboarded_contributor_count <= :onboardedContributorCountMax)
              and (coalesce(:activeContributorCountMin) is null or d.active_contributor_count >= :activeContributorCountMin)
              and (coalesce(:activeContributorCountEq) is null or d.active_contributor_count = :activeContributorCountEq)
              and (coalesce(:activeContributorCountMax) is null or d.active_contributor_count <= :activeContributorCountMax)
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
                    SELECT count(d.project_id)
                    FROM (SELECT d.project_id                                                                          as project_id,
                                 sum(d.granted_usd_amount) - sum(d.rewarded_usd_amount)                                as available_budget,
                                 sum(d.granted_usd_amount) / greatest(sum(d.rewarded_usd_amount), 1)                   as percent_spent_budget,
                                 sum(d.granted_usd_amount)                                                             as total_granted_usd_amount,
                                 count(d.contribution_id)                                                              as contribution_count,
                                 count(d.reward_id)                                                                    as reward_count,
                                 sum(d.rewarded_usd_amount)                                                            as total_rewarded_usd_amount,
                                 avg(d.rewarded_usd_amount)                                                            as average_reward_usd_amount,
                                 count(d.contribution_id) filter ( where d.is_merged_pr is true )                      as merged_pr_count,
                                 count(distinct d.contributor_id)                                                      as active_contributor_count,
                                 count(distinct d.contributor_id) filter ( where d.is_first_contribution_on_onlydust ) as onboarded_contributor_count
                          from bi.project_data_unions d
                          where d.timestamp >= :fromDate
                            and d.timestamp < :toDate
                            and (coalesce(:projectLeadIds) is null or d.project_lead_ids && cast(:projectLeadIds as uuid[]))
                            and (coalesce(:categoryIds) is null or d.project_category_ids && cast(:categoryIds as uuid[]))
                            and (coalesce(:languageIds) is null or d.language_ids && cast(:languageIds as uuid[]))
                            and (coalesce(:ecosystemIds) is null or d.ecosystem_ids && cast(:ecosystemIds as uuid[]))
                          group by d.project_id) d
                    
                    WHERE (coalesce(:availableBudgetUsdAmountMin) is null or d.available_budget >= :availableBudgetUsdAmountMin)
                      and (coalesce(:availableBudgetUsdAmountEq) is null or d.available_budget = :availableBudgetUsdAmountEq)
                      and (coalesce(:availableBudgetUsdAmountMax) is null or d.available_budget <= :availableBudgetUsdAmountMax)
                      and (coalesce(:percentUsedBudgetMin) is null or d.percent_spent_budget >= :percentUsedBudgetMin)
                      and (coalesce(:percentUsedBudgetEq) is null or d.percent_spent_budget = :percentUsedBudgetEq)
                      and (coalesce(:percentUsedBudgetMax) is null or d.percent_spent_budget <= :percentUsedBudgetMax)
                      and (coalesce(:totalGrantedUsdAmountMin) is null or d.total_granted_usd_amount >= :totalGrantedUsdAmountMin)
                      and (coalesce(:totalGrantedUsdAmountEq) is null or d.total_granted_usd_amount = :totalGrantedUsdAmountEq)
                      and (coalesce(:totalGrantedUsdAmountMax) is null or d.total_granted_usd_amount <= :totalGrantedUsdAmountMax)
                      and (coalesce(:averageRewardUsdAmountMin) is null or d.average_reward_usd_amount >= :averageRewardUsdAmountMin)
                      and (coalesce(:averageRewardUsdAmountEq) is null or d.average_reward_usd_amount = :averageRewardUsdAmountEq)
                      and (coalesce(:averageRewardUsdAmountMax) is null or d.average_reward_usd_amount <= :averageRewardUsdAmountMax)
                      and (coalesce(:totalRewardedUsdAmountMin) is null or d.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
                      and (coalesce(:totalRewardedUsdAmountEq) is null or d.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
                      and (coalesce(:totalRewardedUsdAmountMax) is null or d.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
                      and (coalesce(:onboardedContributorCountMin) is null or d.onboarded_contributor_count >= :onboardedContributorCountMin)
                      and (coalesce(:onboardedContributorCountEq) is null or d.onboarded_contributor_count = :onboardedContributorCountEq)
                      and (coalesce(:onboardedContributorCountMax) is null or d.onboarded_contributor_count <= :onboardedContributorCountMax)
                      and (coalesce(:activeContributorCountMin) is null or d.active_contributor_count >= :activeContributorCountMin)
                      and (coalesce(:activeContributorCountEq) is null or d.active_contributor_count = :activeContributorCountEq)
                      and (coalesce(:activeContributorCountMax) is null or d.active_contributor_count <= :activeContributorCountMax)
                      and (coalesce(:mergedPrCountMin) is null or d.merged_pr_count >= :mergedPrCountMin)
                      and (coalesce(:mergedPrCountEq) is null or d.merged_pr_count = :mergedPrCountEq)
                      and (coalesce(:mergedPrCountMax) is null or d.merged_pr_count <= :mergedPrCountMax)
                      and (coalesce(:rewardCountMin) is null or d.reward_count >= :rewardCountMin)
                      and (coalesce(:rewardCountEq) is null or d.reward_count = :rewardCountEq)
                      and (coalesce(:rewardCountMax) is null or d.reward_count <= :rewardCountMax)
                      and (coalesce(:contributionCountMin) is null or d.contribution_count >= :contributionCountMin)
                      and (coalesce(:contributionCountEq) is null or d.contribution_count = :contributionCountEq)
                      and (coalesce(:contributionCountMax) is null or d.contribution_count <= :contributionCountMax)
                    """,
            nativeQuery = true)
    Page<ProjectKpisReadEntity> findAll(@NonNull ZonedDateTime fromDate,
                                        @NonNull ZonedDateTime toDate,
                                        @NonNull ZonedDateTime fromDatePreviousPeriod,
                                        @NonNull ZonedDateTime toDatePreviousPeriod,
                                        UUID[] projectLeadIds,
                                        UUID[] categoryIds,
                                        UUID[] languageIds,
                                        UUID[] ecosystemIds,
                                        BigDecimal availableBudgetUsdAmountMin,
                                        BigDecimal availableBudgetUsdAmountEq,
                                        BigDecimal availableBudgetUsdAmountMax,
                                        Integer percentUsedBudgetMin,
                                        Integer percentUsedBudgetEq,
                                        Integer percentUsedBudgetMax,
                                        BigDecimal totalGrantedUsdAmountMin,
                                        BigDecimal totalGrantedUsdAmountEq,
                                        BigDecimal totalGrantedUsdAmountMax,
                                        BigDecimal averageRewardUsdAmountMin,
                                        BigDecimal averageRewardUsdAmountEq,
                                        BigDecimal averageRewardUsdAmountMax,
                                        BigDecimal totalRewardedUsdAmountMin,
                                        BigDecimal totalRewardedUsdAmountEq,
                                        BigDecimal totalRewardedUsdAmountMax,
                                        Integer onboardedContributorCountMin,
                                        Integer onboardedContributorCountEq,
                                        Integer onboardedContributorCountMax,
                                        Integer activeContributorCountMin,
                                        Integer activeContributorCountEq,
                                        Integer activeContributorCountMax,
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

    static String getSortProperty(ProjectKpiSortEnum sort) {
        return sort == null ? "pgd.project_name" : switch (sort) {
            case PROJECT_NAME -> "pgd.project_name";
            case AVAILABLE_BUDGET_USD_AMOUNT -> "available_budget";
            case PERCENT_USED_BUDGET -> "percent_spent_budget";
            case TOTAL_GRANTED_USD_AMOUNT -> "total_granted_usd_amount";
            case AVERAGE_REWARD_USD_AMOUNT -> "average_reward_usd_amount";
            case TOTAL_REWARDED_USD_AMOUNT -> "total_rewarded_usd_amount";
            case ONBOARDED_CONTRIBUTOR_COUNT -> "onboarded_contributor_count";
            case ACTIVE_CONTRIBUTOR_COUNT -> "active_contributor_count";
            case MERGED_PR_COUNT -> "merged_pr_count";
            case REWARD_COUNT -> "reward_count";
            case CONTRIBUTION_COUNT -> "contribution_count";
        };
    }
}

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

    static String getSortProperty(ProjectKpiSortEnum sort) {
        return sort == null ? "project_name" : switch (sort) {
            case PROJECT_NAME -> "project_name";
            case AVAILABLE_BUDGET_USD_AMOUNT -> "available_budget";
            case PERCENT_USED_BUDGET -> "percent_spent_budget";
            case TOTAL_GRANTED_USD_AMOUNT -> "total_granted_usd_amount";
            case AVERAGE_REWARD_USD_AMOUNT -> "average_reward_usd_amount";
            case TOTAL_REWARDED_USD_AMOUNT -> "total_rewarded_usd_amount";
            case ONBOARDED_CONTRIBUTOR_COUNT -> "onboarded_contributor_count";
            case ACTIVE_CONTRIBUTOR_COUNT -> "active_contributor_count";
            case PR_COUNT -> "completed_pr_count";
            case ISSUE_COUNT -> "completed_issue_count";
            case CODE_REVIEW_COUNT -> "completed_code_review_count";
            case REWARD_COUNT -> "reward_count";
            case CONTRIBUTION_COUNT -> "completed_contribution_count";
        };
    }

    @Query(value = """
            SELECT * -- wrap the query in a subquery to let Hibernate paginate
            FROM (SELECT -- /// global data /// --
                         p.project_id,
                         p.project_name,
                         p.project,
                         p.leads,
                         p.categories,
                         p.languages,
                         p.ecosystems,
                         p.programs,
                         pb.budget,
                         pb.available_budget_usd                                      as available_budget,
                         pb.percent_spent_budget_usd                                  as percent_spent_budget,
                         -- /// filtered & computed data /// --
                         coalesce(gd.total_granted_usd_amount, 0)                     as total_granted_usd_amount,
                         coalesce(cd.completed_contribution_count, 0)                 as completed_contribution_count,
                         coalesce(cd.completed_issue_count, 0)                        as completed_issue_count,
                         coalesce(cd.completed_pr_count, 0)                           as completed_pr_count,
                         coalesce(cd.completed_code_review_count, 0)                  as completed_code_review_count,
                         coalesce(cd.active_contributor_count, 0)                     as active_contributor_count,
                         coalesce(cd.onboarded_contributor_count, 0)                  as onboarded_contributor_count,
                         coalesce(rd.reward_count, 0)                                 as reward_count,
                         coalesce(rd.total_rewarded_usd_amount, 0)                    as total_rewarded_usd_amount,
                         coalesce(rd.average_reward_usd_amount, 0)                    as average_reward_usd_amount,
                         coalesce(gd.previous_period_total_granted_usd_amount, 0)     as previous_period_total_granted_usd_amount,
                         coalesce(cd.previous_period_completed_contribution_count, 0) as previous_period_completed_contribution_count,
                         coalesce(cd.previous_period_completed_issue_count, 0)        as previous_period_completed_issue_count,
                         coalesce(cd.previous_period_completed_pr_count, 0)           as previous_period_completed_pr_count,
                         coalesce(cd.previous_period_completed_code_review_count, 0)  as previous_period_completed_code_review_count,
                         coalesce(cd.previous_period_active_contributor_count, 0)     as previous_period_active_contributor_count,
                         coalesce(cd.previous_period_onboarded_contributor_count, 0)  as previous_period_onboarded_contributor_count,
                         coalesce(rd.previous_period_reward_count, 0)                 as previous_period_reward_count,
                         coalesce(rd.previous_period_total_rewarded_usd_amount, 0)    as previous_period_total_rewarded_usd_amount,
                         coalesce(rd.previous_period_average_reward_usd_amount, 0)    as previous_period_average_reward_usd_amount,
                         engagement_status.value                                      as engagement_status
            
                  FROM bi.p_project_global_data p
                           JOIN bi.p_project_budget_data pb on pb.project_id = p.project_id
            
                           LEFT JOIN (select cd.project_id,
            
                                             count(cd.contribution_uuid) filter ( where cd.timestamp >= :fromDate )                                                as completed_contribution_count,
                                             coalesce(sum(cd.is_issue) filter ( where cd.timestamp >= :fromDate ), 0)                                              as completed_issue_count,
                                             coalesce(sum(cd.is_pr) filter ( where cd.timestamp >= :fromDate ), 0)                                                 as completed_pr_count,
                                             coalesce(sum(cd.is_code_review) filter ( where cd.timestamp >= :fromDate ), 0)                                        as completed_code_review_count,
                                             count(distinct cd.contributor_id) filter ( where cd.timestamp >= :fromDate )                                          as active_contributor_count,
                                             count(distinct cd.contributor_id) filter ( where cd.timestamp >= :fromDate and cd.is_first_contribution_on_onlydust ) as onboarded_contributor_count,
            
                                             count(cd.contribution_uuid) filter ( where cd.timestamp < :fromDate )                                                 as previous_period_completed_contribution_count,
                                             coalesce(sum(cd.is_issue) filter ( where cd.timestamp < :fromDate ), 0)                                               as previous_period_completed_issue_count,
                                             coalesce(sum(cd.is_pr) filter ( where cd.timestamp < :fromDate ), 0)                                                  as previous_period_completed_pr_count,
                                             coalesce(sum(cd.is_code_review) filter ( where cd.timestamp < :fromDate ), 0)                                         as previous_period_completed_code_review_count,
                                             count(distinct cd.contributor_id) filter ( where cd.timestamp < :fromDate )                                           as previous_period_active_contributor_count,
                                             count(distinct cd.contributor_id) filter ( where cd.timestamp < :fromDate and cd.is_first_contribution_on_onlydust )  as previous_period_onboarded_contributor_count
                                      from bi.p_per_contributor_contribution_data cd
                                      where (coalesce(:fromDatePreviousPeriod) is null or cd.timestamp >= :fromDatePreviousPeriod)
                                        and (coalesce(:toDate) is null or cd.timestamp < :toDate)
                                        and (not :filteredKpis or coalesce(:languageIds) is null or cd.language_ids && :languageIds)
                                        and cd.contribution_status = 'COMPLETED'
                                        and cd.project_id is not null
                                      group by cd.project_id) cd
                                     on cd.project_id = p.project_id
            
                           LEFT JOIN (select rd.project_id,
            
                                             count(rd.reward_id) filter ( where rd.timestamp >= :fromDate )             as reward_count,
                                             coalesce(sum(rd.usd_amount) filter ( where rd.timestamp >= :fromDate ), 0) as total_rewarded_usd_amount,
                                             coalesce(avg(rd.usd_amount) filter ( where rd.timestamp >= :fromDate ), 0) as average_reward_usd_amount,
            
                                             count(rd.reward_id) filter ( where rd.timestamp < :fromDate )              as previous_period_reward_count,
                                             coalesce(sum(rd.usd_amount) filter ( where rd.timestamp < :fromDate ), 0)  as previous_period_total_rewarded_usd_amount,
                                             coalesce(avg(rd.usd_amount) filter ( where rd.timestamp < :fromDate ), 0)  as previous_period_average_reward_usd_amount
                                      from bi.p_reward_data rd
                                      where (coalesce(:fromDatePreviousPeriod) is null or rd.timestamp >= :fromDatePreviousPeriod)
                                        and (coalesce(:toDate) is null or rd.timestamp < :toDate)
                                        and (not :filteredKpis or coalesce(:projectLeadIds) is null or rd.requestor_id = any (:projectLeadIds))
                                        and (not :filteredKpis or coalesce(:languageIds) is null or rd.language_ids && :languageIds)
                                      group by rd.project_id) rd on rd.project_id = p.project_id
            
                           LEFT JOIN (select gd.project_id,
                                             coalesce(sum(gd.usd_amount) filter ( where gd.timestamp >= :fromDate ), 0) as total_granted_usd_amount,
                                             coalesce(sum(gd.usd_amount) filter ( where gd.timestamp < :fromDate ), 0)  as previous_period_total_granted_usd_amount
                                      from bi.p_project_grants_data gd
                                      where (coalesce(:fromDatePreviousPeriod) is null or gd.timestamp >= :fromDatePreviousPeriod)
                                        and (coalesce(:toDate) is null or gd.timestamp < :toDate)
                                      group by gd.project_id) gd on gd.project_id = p.project_id
            
                           LEFT JOIN LATERAL ( select cast(case
                                                               when coalesce(cd.previous_period_completed_contribution_count, 0) > 0 and
                                                                    coalesce(cd.completed_contribution_count, 0) > 0
                                                                   then 'ACTIVE'
                                                               when coalesce(cd.previous_period_completed_contribution_count, 0) > 0 and
                                                                    coalesce(cd.completed_contribution_count, 0) = 0
                                                                   then 'CHURNED'
                                                               when coalesce(cd.previous_period_completed_contribution_count, 0) = 0 and
                                                                    coalesce(cd.completed_contribution_count, 0) > 0
                                                                   then case
                                                                            when exists(select 1 from bi.p_contribution_data cd where cd.project_id = p.project_id and cd.contribution_status = 'COMPLETED' and cd.timestamp < :fromDatePreviousPeriod)
                                                                                then 'REACTIVATED'
                                                                            else 'NEW' end
                                                               else 'INACTIVE' end as engagement_status) as value) engagement_status ON true
            
                  WHERE (cd.project_id is not null or rd.project_id is not null or gd.project_id is not null)
                    and (coalesce(:dataSourceIds) is null or p.project_id = any (:dataSourceIds) or p.program_ids && :dataSourceIds or p.ecosystem_ids && :dataSourceIds)
                    and (coalesce(:ecosystemIds) is null or p.ecosystem_ids && :ecosystemIds)
                    and (coalesce(:programIds) is null or p.program_ids && :programIds)
                    and (coalesce(:projectIds) is null or p.project_id = any (:projectIds))
                    and (coalesce(:projectSlugs) is null or p.project_slug = any (cast(:projectSlugs as text[])))
                    and (coalesce(:projectLeadIds) is null or p.project_lead_ids && :projectLeadIds)
                    and (coalesce(:categoryIds) is null or p.project_category_ids && :categoryIds)
                    and (coalesce(:languageIds) is null or p.language_ids && :languageIds)
                    and (coalesce(:searchQuery) is null or p.search ilike '%' || :searchQuery || '%')
                    and (coalesce(:availableBudgetUsdAmountMin) is null or pb.available_budget_usd >= :availableBudgetUsdAmountMin)
                    and (coalesce(:availableBudgetUsdAmountEq) is null or pb.available_budget_usd = :availableBudgetUsdAmountEq)
                    and (coalesce(:availableBudgetUsdAmountMax) is null or pb.available_budget_usd <= :availableBudgetUsdAmountMax)
                    and (coalesce(:percentUsedBudgetMin) is null or pb.percent_spent_budget_usd >= :percentUsedBudgetMin)
                    and (coalesce(:percentUsedBudgetEq) is null or pb.percent_spent_budget_usd = :percentUsedBudgetEq)
                    and (coalesce(:percentUsedBudgetMax) is null or pb.percent_spent_budget_usd <= :percentUsedBudgetMax)
                    and (coalesce(:totalGrantedUsdAmountMin) is null or gd.total_granted_usd_amount >= :totalGrantedUsdAmountMin)
                    and (coalesce(:totalGrantedUsdAmountEq) is null or gd.total_granted_usd_amount = :totalGrantedUsdAmountEq)
                    and (coalesce(:totalGrantedUsdAmountMax) is null or gd.total_granted_usd_amount <= :totalGrantedUsdAmountMax)
                    and (coalesce(:averageRewardUsdAmountMin) is null or rd.average_reward_usd_amount >= :averageRewardUsdAmountMin)
                    and (coalesce(:averageRewardUsdAmountEq) is null or rd.average_reward_usd_amount = :averageRewardUsdAmountEq)
                    and (coalesce(:averageRewardUsdAmountMax) is null or rd.average_reward_usd_amount <= :averageRewardUsdAmountMax)
                    and (coalesce(:totalRewardedUsdAmountMin) is null or rd.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
                    and (coalesce(:totalRewardedUsdAmountEq) is null or rd.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
                    and (coalesce(:totalRewardedUsdAmountMax) is null or rd.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
                    and (coalesce(:rewardCountMin) is null or rd.reward_count >= :rewardCountMin)
                    and (coalesce(:rewardCountEq) is null or rd.reward_count = :rewardCountEq)
                    and (coalesce(:rewardCountMax) is null or rd.reward_count <= :rewardCountMax)
                    and (coalesce(:onboardedContributorCountMin) is null or cd.onboarded_contributor_count >= :onboardedContributorCountMin)
                    and (coalesce(:onboardedContributorCountEq) is null or cd.onboarded_contributor_count = :onboardedContributorCountEq)
                    and (coalesce(:onboardedContributorCountMax) is null or cd.onboarded_contributor_count <= :onboardedContributorCountMax)
                    and (coalesce(:activeContributorCountMin) is null or cd.active_contributor_count >= :activeContributorCountMin)
                    and (coalesce(:activeContributorCountEq) is null or cd.active_contributor_count = :activeContributorCountEq)
                    and (coalesce(:activeContributorCountMax) is null or cd.active_contributor_count <= :activeContributorCountMax)
                    and (coalesce(:completedContributionCountMin) is null or
                         (
                             case when 'ISSUE' = any (:contributionTypes) then cd.completed_issue_count else 0 end +
                             case when 'PULL_REQUEST' = any (:contributionTypes) then cd.completed_pr_count else 0 end +
                             case when 'CODE_REVIEW' = any (:contributionTypes) then cd.completed_code_review_count else 0 end
                             ) >= :completedContributionCountMin)
                    and (coalesce(:completedContributionCountEq) is null or
                         (
                             case when 'ISSUE' = any (:contributionTypes) then cd.completed_issue_count else 0 end +
                             case when 'PULL_REQUEST' = any (:contributionTypes) then cd.completed_pr_count else 0 end +
                             case when 'CODE_REVIEW' = any (:contributionTypes) then cd.completed_code_review_count else 0 end
                             ) = :completedContributionCountEq)
                    and (coalesce(:completedContributionCountMax) is null or
                         (
                             case when 'ISSUE' = any (:contributionTypes) then cd.completed_issue_count else 0 end +
                             case when 'PULL_REQUEST' = any (:contributionTypes) then cd.completed_pr_count else 0 end +
                             case when 'CODE_REVIEW' = any (:contributionTypes) then cd.completed_code_review_count else 0 end
                             ) <= :completedContributionCountMax)
                    and (coalesce(:engagementStatuses) is null or engagement_status.value = any (cast(:engagementStatuses as engagement_status[])))) d
            """,
            countQuery = """
                          SELECT count(*)
                    
                          FROM bi.p_project_global_data p
                                   JOIN bi.p_project_budget_data pb on pb.project_id = p.project_id
                    
                                   LEFT JOIN (select cd.project_id,
                    
                                                     count(cd.contribution_uuid) filter ( where cd.timestamp >= :fromDate )                                                as completed_contribution_count,
                                                     coalesce(sum(cd.is_issue) filter ( where cd.timestamp >= :fromDate ), 0)                                              as completed_issue_count,
                                                     coalesce(sum(cd.is_pr) filter ( where cd.timestamp >= :fromDate ), 0)                                                 as completed_pr_count,
                                                     coalesce(sum(cd.is_code_review) filter ( where cd.timestamp >= :fromDate ), 0)                                        as completed_code_review_count,
                                                     count(distinct cd.contributor_id) filter ( where cd.timestamp >= :fromDate )                                          as active_contributor_count,
                                                     count(distinct cd.contributor_id) filter ( where cd.timestamp >= :fromDate and cd.is_first_contribution_on_onlydust ) as onboarded_contributor_count,
                    
                                                     count(cd.contribution_uuid) filter ( where cd.timestamp < :fromDate )                                                 as previous_period_completed_contribution_count,
                                                     coalesce(sum(cd.is_issue) filter ( where cd.timestamp < :fromDate ), 0)                                               as previous_period_completed_issue_count,
                                                     coalesce(sum(cd.is_pr) filter ( where cd.timestamp < :fromDate ), 0)                                                  as previous_period_completed_pr_count,
                                                     coalesce(sum(cd.is_code_review) filter ( where cd.timestamp < :fromDate ), 0)                                         as previous_period_completed_code_review_count,
                                                     count(distinct cd.contributor_id) filter ( where cd.timestamp < :fromDate )                                           as previous_period_active_contributor_count,
                                                     count(distinct cd.contributor_id) filter ( where cd.timestamp < :fromDate and cd.is_first_contribution_on_onlydust )  as previous_period_onboarded_contributor_count
                                              from bi.p_per_contributor_contribution_data cd
                                              where (coalesce(:fromDatePreviousPeriod) is null or cd.timestamp >= :fromDatePreviousPeriod)
                                                and (coalesce(:toDate) is null or cd.timestamp < :toDate)
                                                and (not :filteredKpis or coalesce(:languageIds) is null or cd.language_ids && :languageIds)
                                                and cd.contribution_status = 'COMPLETED'
                                                and cd.project_id is not null
                                              group by cd.project_id) cd
                                             on cd.project_id = p.project_id
                    
                                   LEFT JOIN (select rd.project_id,
                    
                                                     count(rd.reward_id) filter ( where rd.timestamp >= :fromDate )             as reward_count,
                                                     coalesce(sum(rd.usd_amount) filter ( where rd.timestamp >= :fromDate ), 0) as total_rewarded_usd_amount,
                                                     coalesce(avg(rd.usd_amount) filter ( where rd.timestamp >= :fromDate ), 0) as average_reward_usd_amount,
                    
                                                     count(rd.reward_id) filter ( where rd.timestamp < :fromDate )              as previous_period_reward_count,
                                                     coalesce(sum(rd.usd_amount) filter ( where rd.timestamp < :fromDate ), 0)  as previous_period_total_rewarded_usd_amount,
                                                     coalesce(avg(rd.usd_amount) filter ( where rd.timestamp < :fromDate ), 0)  as previous_period_average_reward_usd_amount
                                              from bi.p_reward_data rd
                                              where (coalesce(:fromDatePreviousPeriod) is null or rd.timestamp >= :fromDatePreviousPeriod)
                                                and (coalesce(:toDate) is null or rd.timestamp < :toDate)
                                                and (not :filteredKpis or coalesce(:projectLeadIds) is null or rd.requestor_id = any (:projectLeadIds))
                                                and (not :filteredKpis or coalesce(:languageIds) is null or rd.language_ids && :languageIds)
                                              group by rd.project_id) rd on rd.project_id = p.project_id
                    
                                   LEFT JOIN (select gd.project_id,
                                                     coalesce(sum(gd.usd_amount) filter ( where gd.timestamp >= :fromDate ), 0) as total_granted_usd_amount,
                                                     coalesce(sum(gd.usd_amount) filter ( where gd.timestamp < :fromDate ), 0)  as previous_period_total_granted_usd_amount
                                              from bi.p_project_grants_data gd
                                              where (coalesce(:fromDatePreviousPeriod) is null or gd.timestamp >= :fromDatePreviousPeriod)
                                                and (coalesce(:toDate) is null or gd.timestamp < :toDate)
                                              group by gd.project_id) gd on gd.project_id = p.project_id
                    
                                   LEFT JOIN LATERAL ( select cast(case
                                                                       when coalesce(cd.previous_period_completed_contribution_count, 0) > 0 and
                                                                            coalesce(cd.completed_contribution_count, 0) > 0
                                                                           then 'ACTIVE'
                                                                       when coalesce(cd.previous_period_completed_contribution_count, 0) > 0 and
                                                                            coalesce(cd.completed_contribution_count, 0) = 0
                                                                           then 'CHURNED'
                                                                       when coalesce(cd.previous_period_completed_contribution_count, 0) = 0 and
                                                                            coalesce(cd.completed_contribution_count, 0) > 0
                                                                           then case
                                                                                    when exists(select 1 from bi.p_contribution_data cd where cd.project_id = p.project_id and cd.contribution_status = 'COMPLETED' and cd.timestamp < :fromDatePreviousPeriod)
                                                                                        then 'REACTIVATED'
                                                                                    else 'NEW' end
                                                                       else 'INACTIVE' end as engagement_status) as value) engagement_status ON true
                    
                          WHERE (cd.project_id is not null or rd.project_id is not null or gd.project_id is not null)
                            and (coalesce(:dataSourceIds) is null or p.project_id = any (:dataSourceIds) or p.program_ids && :dataSourceIds or p.ecosystem_ids && :dataSourceIds)
                            and (coalesce(:ecosystemIds) is null or p.ecosystem_ids && :ecosystemIds)
                            and (coalesce(:programIds) is null or p.program_ids && :programIds)
                            and (coalesce(:projectIds) is null or p.project_id = any (:projectIds))
                            and (coalesce(:projectSlugs) is null or p.project_slug = any (cast(:projectSlugs as text[])))
                            and (coalesce(:projectLeadIds) is null or p.project_lead_ids && :projectLeadIds)
                            and (coalesce(:categoryIds) is null or p.project_category_ids && :categoryIds)
                            and (coalesce(:languageIds) is null or p.language_ids && :languageIds)
                            and (coalesce(:searchQuery) is null or p.search ilike '%' || :searchQuery || '%')
                            and (coalesce(:availableBudgetUsdAmountMin) is null or pb.available_budget_usd >= :availableBudgetUsdAmountMin)
                            and (coalesce(:availableBudgetUsdAmountEq) is null or pb.available_budget_usd = :availableBudgetUsdAmountEq)
                            and (coalesce(:availableBudgetUsdAmountMax) is null or pb.available_budget_usd <= :availableBudgetUsdAmountMax)
                            and (coalesce(:percentUsedBudgetMin) is null or pb.percent_spent_budget_usd >= :percentUsedBudgetMin)
                            and (coalesce(:percentUsedBudgetEq) is null or pb.percent_spent_budget_usd = :percentUsedBudgetEq)
                            and (coalesce(:percentUsedBudgetMax) is null or pb.percent_spent_budget_usd <= :percentUsedBudgetMax)
                            and (coalesce(:totalGrantedUsdAmountMin) is null or gd.total_granted_usd_amount >= :totalGrantedUsdAmountMin)
                            and (coalesce(:totalGrantedUsdAmountEq) is null or gd.total_granted_usd_amount = :totalGrantedUsdAmountEq)
                            and (coalesce(:totalGrantedUsdAmountMax) is null or gd.total_granted_usd_amount <= :totalGrantedUsdAmountMax)
                            and (coalesce(:averageRewardUsdAmountMin) is null or rd.average_reward_usd_amount >= :averageRewardUsdAmountMin)
                            and (coalesce(:averageRewardUsdAmountEq) is null or rd.average_reward_usd_amount = :averageRewardUsdAmountEq)
                            and (coalesce(:averageRewardUsdAmountMax) is null or rd.average_reward_usd_amount <= :averageRewardUsdAmountMax)
                            and (coalesce(:totalRewardedUsdAmountMin) is null or rd.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
                            and (coalesce(:totalRewardedUsdAmountEq) is null or rd.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
                            and (coalesce(:totalRewardedUsdAmountMax) is null or rd.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
                            and (coalesce(:rewardCountMin) is null or rd.reward_count >= :rewardCountMin)
                            and (coalesce(:rewardCountEq) is null or rd.reward_count = :rewardCountEq)
                            and (coalesce(:rewardCountMax) is null or rd.reward_count <= :rewardCountMax)
                            and (coalesce(:onboardedContributorCountMin) is null or cd.onboarded_contributor_count >= :onboardedContributorCountMin)
                            and (coalesce(:onboardedContributorCountEq) is null or cd.onboarded_contributor_count = :onboardedContributorCountEq)
                            and (coalesce(:onboardedContributorCountMax) is null or cd.onboarded_contributor_count <= :onboardedContributorCountMax)
                            and (coalesce(:activeContributorCountMin) is null or cd.active_contributor_count >= :activeContributorCountMin)
                            and (coalesce(:activeContributorCountEq) is null or cd.active_contributor_count = :activeContributorCountEq)
                            and (coalesce(:activeContributorCountMax) is null or cd.active_contributor_count <= :activeContributorCountMax)
                            and (coalesce(:completedContributionCountMin) is null or
                                 (
                                     case when 'ISSUE' = any (:contributionTypes) then cd.completed_issue_count else 0 end +
                                     case when 'PULL_REQUEST' = any (:contributionTypes) then cd.completed_pr_count else 0 end +
                                     case when 'CODE_REVIEW' = any (:contributionTypes) then cd.completed_code_review_count else 0 end
                                     ) >= :completedContributionCountMin)
                            and (coalesce(:completedContributionCountEq) is null or
                                 (
                                     case when 'ISSUE' = any (:contributionTypes) then cd.completed_issue_count else 0 end +
                                     case when 'PULL_REQUEST' = any (:contributionTypes) then cd.completed_pr_count else 0 end +
                                     case when 'CODE_REVIEW' = any (:contributionTypes) then cd.completed_code_review_count else 0 end
                                     ) = :completedContributionCountEq)
                            and (coalesce(:completedContributionCountMax) is null or
                                 (
                                     case when 'ISSUE' = any (:contributionTypes) then cd.completed_issue_count else 0 end +
                                     case when 'PULL_REQUEST' = any (:contributionTypes) then cd.completed_pr_count else 0 end +
                                     case when 'CODE_REVIEW' = any (:contributionTypes) then cd.completed_code_review_count else 0 end
                                     ) <= :completedContributionCountMax)
                            and (coalesce(:engagementStatuses) is null or engagement_status.value = any (cast(:engagementStatuses as engagement_status[])))
                    """,
            nativeQuery = true)
    Page<ProjectKpisReadEntity> findAll(@NonNull ZonedDateTime fromDate,
                                        @NonNull ZonedDateTime toDate,
                                        @NonNull ZonedDateTime fromDatePreviousPeriod,
                                        @NonNull UUID[] dataSourceIds,
                                        @NonNull Boolean filteredKpis,
                                        String searchQuery,
                                        UUID[] programIds,
                                        UUID[] projectIds,
                                        String[] projectSlugs,
                                        UUID[] projectLeadIds,
                                        UUID[] categoryIds,
                                        UUID[] languageIds,
                                        UUID[] ecosystemIds,
                                        BigDecimal availableBudgetUsdAmountMin,
                                        BigDecimal availableBudgetUsdAmountEq,
                                        BigDecimal availableBudgetUsdAmountMax,
                                        BigDecimal percentUsedBudgetMin,
                                        BigDecimal percentUsedBudgetEq,
                                        BigDecimal percentUsedBudgetMax,
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
                                        Integer rewardCountMin,
                                        Integer rewardCountEq,
                                        Integer rewardCountMax,
                                        Integer completedContributionCountMin,
                                        Integer completedContributionCountEq,
                                        Integer completedContributionCountMax,
                                        String[] contributionTypes,
                                        String[] engagementStatuses,
                                        Pageable pageable);
}

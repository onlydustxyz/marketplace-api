package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.bi.ContributorKpisReadEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.parseZonedNullable;

public interface ContributorKpisReadRepository extends Repository<ContributorKpisReadEntity, Long> {
    @Language("PostgreSQL")
    String SELECT = """
            SELECT -- /// global data /// --
                   coalesce(d.contributor_id, previous_period.contributor_id)           as contributor_id,
                   coalesce(d.contributor_login, previous_period.contributor_login)     as contributor_login,
                   coalesce(d.contributor_country, previous_period.contributor_country) as contributor_country,
                   coalesce(d.contributor, previous_period.contributor)                 as contributor,
                   coalesce(d.projects, previous_period.projects)                       as projects,
                   coalesce(d.categories, previous_period.categories)                   as categories,
                   coalesce(d.languages, previous_period.languages)                     as languages,
                   coalesce(d.ecosystems, previous_period.ecosystems)                   as ecosystems,
                   coalesce(d.maintained_projects, previous_period.maintained_projects) as maintained_projects,
                   contributor_labels.list                                              as project_contributor_labels,
                   -- /// filtered & computed data /// --
                   coalesce(d.total_rewarded_usd_amount, 0)                             as total_rewarded_usd_amount,
                   coalesce(d.reward_count, 0)                                          as reward_count,
                   coalesce(d.completed_contribution_count, 0)                          as completed_contribution_count,
                   coalesce(d.completed_issue_count, 0)                                 as completed_issue_count,
                   coalesce(d.completed_pr_count, 0)                                    as completed_pr_count,
                   coalesce(d.completed_code_review_count, 0)                           as completed_code_review_count,
                   coalesce(d.in_progress_issue_count, 0)                               as in_progress_issue_count,
                   coalesce(d.pending_application_count, 0)                             as pending_application_count,
                   coalesce(previous_period.total_rewarded_usd_amount, 0)               as previous_period_total_rewarded_usd_amount,
                   coalesce(previous_period.reward_count, 0)                            as previous_period_reward_count,
                   coalesce(previous_period.completed_contribution_count, 0)            as previous_period_completed_contribution_count,
                   coalesce(previous_period.completed_issue_count, 0)                   as previous_period_completed_issue_count,
                   coalesce(previous_period.completed_pr_count, 0)                      as previous_period_completed_pr_count,
                   coalesce(previous_period.completed_code_review_count, 0)             as previous_period_completed_code_review_count,
                   coalesce(previous_period.in_progress_issue_count, 0)                 as previous_period_in_progress_issue_count,
                   coalesce(previous_period.pending_application_count, 0)               as previous_period_pending_application_count,
                   activity_status.value                                                as activity_status
            
            FROM bi.select_contributors(:fromDate, :toDate, :onlyDustContributionsOnly, :dataSourceIds,
                                        :contributorIds, :contributionUuids, :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes,
                                        cast(:contributionStatuses as indexer_exp.contribution_status[]), :search, :showFilteredKpis, :includeApplicants) d
            
                     LEFT JOIN (select *
                                from bi.select_contributors(:fromDatePreviousPeriod, :toDatePreviousPeriod, :onlyDustContributionsOnly, :dataSourceIds,
                                                            :contributorIds, :contributionUuids, :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes,
                                                            cast(:contributionStatuses as indexer_exp.contribution_status[]), :search, :showFilteredKpis, :includeApplicants)) previous_period
                               ON coalesce(:fromDatePreviousPeriod, :toDatePreviousPeriod) is not null and previous_period.contributor_id = d.contributor_id
            
                     LEFT JOIN LATERAL ( select cast(case
                                                         when coalesce(previous_period.od_completed_contribution_count, 0) > 0 and
                                                              coalesce(d.od_completed_contribution_count, 0) > 0
                                                             then 'ACTIVE'
                                                         when coalesce(previous_period.od_completed_contribution_count, 0) > 0 and
                                                              coalesce(d.od_completed_contribution_count, 0) = 0
                                                             then 'CHURNED'
                                                         when coalesce(previous_period.od_completed_contribution_count, 0) = 0 and
                                                              coalesce(d.includes_first_contribution_on_onlydust, false) and
                                                              coalesce(d.od_completed_contribution_count, 0) > 0
                                                             then 'NEW'
                                                         when coalesce(previous_period.od_completed_contribution_count, 0) = 0 and
                                                              not coalesce(d.includes_first_contribution_on_onlydust, false) and
                                                              coalesce(d.od_completed_contribution_count, 0) > 0
                                                             then 'REACTIVATED'
                                                         else 'INACTIVE' end as contributor_activity_status) as value) activity_status ON coalesce(:fromDatePreviousPeriod, :toDatePreviousPeriod) is not null
            
                     LEFT JOIN LATERAL ( select jsonb_agg(jsonb_build_object('id', pcl.id, 'slug', pcl.slug, 'name', pcl.name)) as list
                                         from contributor_project_contributor_labels cpcl
                                                  join project_contributor_labels pcl on pcl.id = cpcl.label_id
                                                  join projects p on p.id = pcl.project_id
                                         where cpcl.github_user_id = coalesce(d.contributor_id, previous_period.contributor_id)
                                           and (coalesce(:projectIds) is null or p.id = any (:projectIds))
                                           and (coalesce(:labelProjectIds) is null or p.id = any (:labelProjectIds))
                                           and (coalesce(:projectSlugs) is null or p.slug = any (:projectSlugs))) contributor_labels ON cast(:projectIds as uuid[]) is not null or
                                                                                                                                        cast(:labelProjectIds as uuid[]) is not null or
                                                                                                                                        cast(:projectSlugs as text[]) is not null
            
            WHERE (activity_status.value != 'INACTIVE' or d.contribution_count > 0 or d.reward_count > 0 or d.pending_application_count > 0)
              and (coalesce(:totalRewardedUsdAmountMin) is null or d.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
              and (coalesce(:totalRewardedUsdAmountEq) is null or d.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
              and (coalesce(:totalRewardedUsdAmountMax) is null or d.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
              and (coalesce(:rewardCountMin) is null or d.reward_count >= :rewardCountMin)
              and (coalesce(:rewardCountEq) is null or d.reward_count = :rewardCountEq)
              and (coalesce(:rewardCountMax) is null or d.reward_count <= :rewardCountMax)
              and (coalesce(:completedContributionCountMin) is null or
                   (
                       case when 'ISSUE' = any (:contributionTypes) then d.completed_issue_count else 0 end +
                       case when 'PULL_REQUEST' = any (:contributionTypes) then d.completed_pr_count else 0 end +
                       case when 'CODE_REVIEW' = any (:contributionTypes) then d.completed_code_review_count else 0 end
                       ) >= :completedContributionCountMin)
              and (coalesce(:completedContributionCountEq) is null or
                   (
                       case when 'ISSUE' = any (:contributionTypes) then d.completed_issue_count else 0 end +
                       case when 'PULL_REQUEST' = any (:contributionTypes) then d.completed_pr_count else 0 end +
                       case when 'CODE_REVIEW' = any (:contributionTypes) then d.completed_code_review_count else 0 end
                       ) = :completedContributionCountEq)
              and (coalesce(:completedContributionCountMax) is null or
                   (
                       case when 'ISSUE' = any (:contributionTypes) then d.completed_issue_count else 0 end +
                       case when 'PULL_REQUEST' = any (:contributionTypes) then d.completed_pr_count else 0 end +
                       case when 'CODE_REVIEW' = any (:contributionTypes) then d.completed_code_review_count else 0 end
                       ) <= :completedContributionCountMax)
              and (coalesce(:activityStatuses) is null or activity_status.value = any (cast(:activityStatuses as contributor_activity_status[])))
            """;

    static String getSortProperty(ContributorKpiSortEnum sort) {
        return sort == null ? "contributor_login" : switch (sort) {
            case CONTRIBUTOR_LOGIN -> "contributor_login";
            case PROJECT_NAME -> "first_project_name";
            case TOTAL_REWARDED_USD_AMOUNT -> "total_rewarded_usd_amount";
            case ISSUE_COUNT -> "completed_issue_count";
            case PR_COUNT -> "completed_pr_count";
            case CODE_REVIEW_COUNT -> "completed_code_review_count";
            case CONTRIBUTION_COUNT -> "completed_contribution_count";
        };
    }

    @Query(value = SELECT,
            countQuery = """
                    SELECT count(*)
                    FROM bi.select_contributors(:fromDate, :toDate, :onlyDustContributionsOnly, :dataSourceIds,
                                        :contributorIds, :contributionUuids, :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes,
                                        cast(:contributionStatuses as indexer_exp.contribution_status[]), :search, :showFilteredKpis, :includeApplicants) d
                    
                             LEFT JOIN (select *
                                        from bi.select_contributors(:fromDatePreviousPeriod, :toDatePreviousPeriod, :onlyDustContributionsOnly, :dataSourceIds,
                                                                    :contributorIds, :contributionUuids, :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes,
                                                                    cast(:contributionStatuses as indexer_exp.contribution_status[]), :search, :showFilteredKpis, :includeApplicants)) previous_period
                                       ON coalesce(:fromDatePreviousPeriod, :toDatePreviousPeriod) is not null and previous_period.contributor_id = d.contributor_id
                    
                             LEFT JOIN LATERAL ( select cast(case
                                                                 when coalesce(previous_period.od_completed_contribution_count, 0) > 0 and
                                                                      coalesce(d.od_completed_contribution_count, 0) > 0
                                                                     then 'ACTIVE'
                                                                 when coalesce(previous_period.od_completed_contribution_count, 0) > 0 and
                                                                      coalesce(d.od_completed_contribution_count, 0) = 0
                                                                     then 'CHURNED'
                                                                 when coalesce(previous_period.od_completed_contribution_count, 0) = 0 and
                                                                      coalesce(d.includes_first_contribution_on_onlydust, false) and
                                                                      coalesce(d.od_completed_contribution_count, 0) > 0
                                                                     then 'NEW'
                                                                 when coalesce(previous_period.od_completed_contribution_count, 0) = 0 and
                                                                      not coalesce(d.includes_first_contribution_on_onlydust, false) and
                                                                      coalesce(d.od_completed_contribution_count, 0) > 0
                                                                     then 'REACTIVATED'
                                                                 else 'INACTIVE' end as contributor_activity_status) as value) activity_status ON coalesce(:fromDatePreviousPeriod, :toDatePreviousPeriod) is not null
                    
                             LEFT JOIN LATERAL ( select jsonb_agg(jsonb_build_object('id', pcl.id, 'slug', pcl.slug, 'name', pcl.name)) as list
                                                 from contributor_project_contributor_labels cpcl
                                                          join project_contributor_labels pcl on pcl.id = cpcl.label_id
                                                          join projects p on p.id = pcl.project_id
                                                 where cpcl.github_user_id = coalesce(d.contributor_id, previous_period.contributor_id)
                                                   and (coalesce(:projectIds) is null or p.id = any (:projectIds))
                                                   and (coalesce(:labelProjectIds) is null or p.id = any (:labelProjectIds))
                                                   and (coalesce(:projectSlugs) is null or p.slug = any (:projectSlugs))) contributor_labels ON cast(:projectIds as uuid[]) is not null or
                                                                                                                                                cast(:labelProjectIds as uuid[]) is not null or
                                                                                                                                                cast(:projectSlugs as text[]) is not null
                    
                    WHERE (activity_status.value != 'INACTIVE' or d.contribution_count > 0 or d.reward_count > 0 or d.pending_application_count > 0)
                      and (coalesce(:totalRewardedUsdAmountMin) is null or d.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
                      and (coalesce(:totalRewardedUsdAmountEq) is null or d.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
                      and (coalesce(:totalRewardedUsdAmountMax) is null or d.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
                      and (coalesce(:rewardCountMin) is null or d.reward_count >= :rewardCountMin)
                      and (coalesce(:rewardCountEq) is null or d.reward_count = :rewardCountEq)
                      and (coalesce(:rewardCountMax) is null or d.reward_count <= :rewardCountMax)
                      and (coalesce(:completedContributionCountMin) is null or
                           (
                               case when 'ISSUE' = any (:contributionTypes) then d.completed_issue_count else 0 end +
                               case when 'PULL_REQUEST' = any (:contributionTypes) then d.completed_pr_count else 0 end +
                               case when 'CODE_REVIEW' = any (:contributionTypes) then d.completed_code_review_count else 0 end
                               ) >= :completedContributionCountMin)
                      and (coalesce(:completedContributionCountEq) is null or
                           (
                               case when 'ISSUE' = any (:contributionTypes) then d.completed_issue_count else 0 end +
                               case when 'PULL_REQUEST' = any (:contributionTypes) then d.completed_pr_count else 0 end +
                               case when 'CODE_REVIEW' = any (:contributionTypes) then d.completed_code_review_count else 0 end
                               ) = :completedContributionCountEq)
                      and (coalesce(:completedContributionCountMax) is null or
                           (
                               case when 'ISSUE' = any (:contributionTypes) then d.completed_issue_count else 0 end +
                               case when 'PULL_REQUEST' = any (:contributionTypes) then d.completed_pr_count else 0 end +
                               case when 'CODE_REVIEW' = any (:contributionTypes) then d.completed_code_review_count else 0 end
                               ) <= :completedContributionCountMax)
                      and (coalesce(:activityStatuses) is null or activity_status.value = any (cast(:activityStatuses as contributor_activity_status[])))
                    """,
            nativeQuery = true)
    Page<ContributorKpisReadEntity> findAll(ZonedDateTime fromDate,
                                            ZonedDateTime toDate,
                                            ZonedDateTime fromDatePreviousPeriod,
                                            ZonedDateTime toDatePreviousPeriod,
                                            @NonNull Boolean onlyDustContributionsOnly,
                                            UUID[] dataSourceIds,
                                            @NonNull Boolean showFilteredKpis,
                                            String search,
                                            Long[] contributorIds,
                                            UUID[] contributionUuids,
                                            UUID[] projectIds,
                                            UUID[] labelProjectIds,
                                            String[] projectSlugs,
                                            UUID[] categoryIds,
                                            UUID[] languageIds,
                                            UUID[] ecosystemIds,
                                            String[] countryCodes,
                                            String[] contributionStatuses,
                                            Boolean includeApplicants,
                                            BigDecimal totalRewardedUsdAmountMin,
                                            BigDecimal totalRewardedUsdAmountEq,
                                            BigDecimal totalRewardedUsdAmountMax,
                                            Integer rewardCountMin,
                                            Integer rewardCountEq,
                                            Integer rewardCountMax,
                                            Integer completedContributionCountMin,
                                            Integer completedContributionCountEq,
                                            Integer completedContributionCountMax,
                                            String[] contributionTypes,
                                            String[] activityStatuses,
                                            Pageable pageable);

    @Query(value = SELECT + " and d.contributor_id = :contributorId", nativeQuery = true)
    Optional<ContributorKpisReadEntity> findById(Long contributorId,
                                                 ZonedDateTime fromDate,
                                                 ZonedDateTime toDate,
                                                 ZonedDateTime fromDatePreviousPeriod,
                                                 ZonedDateTime toDatePreviousPeriod,
                                                 @NonNull Boolean onlyDustContributionsOnly,
                                                 UUID[] dataSourceIds,
                                                 @NonNull Boolean showFilteredKpis,
                                                 String search,
                                                 Long[] contributorIds,
                                                 UUID[] contributionUuids,
                                                 UUID[] projectIds,
                                                 UUID[] labelProjectIds,
                                                 String[] projectSlugs,
                                                 UUID[] categoryIds,
                                                 UUID[] languageIds,
                                                 UUID[] ecosystemIds,
                                                 String[] countryCodes,
                                                 String[] contributionStatuses,
                                                 Boolean includeApplicants,
                                                 BigDecimal totalRewardedUsdAmountMin,
                                                 BigDecimal totalRewardedUsdAmountEq,
                                                 BigDecimal totalRewardedUsdAmountMax,
                                                 Integer rewardCountMin,
                                                 Integer rewardCountEq,
                                                 Integer rewardCountMax,
                                                 Integer completedContributionCountMin,
                                                 Integer completedContributionCountEq,
                                                 Integer completedContributionCountMax,
                                                 String[] contributionTypes,
                                                 String[] activityStatuses);

    default Optional<ContributorKpisReadEntity> findById(Long contributorId) {
        return findById(contributorId, null, null, null, null,
                false, // onlyDustContributionsOnly
                null,
                false, // showFilteredKpis
                null,
                new Long[]{contributorId}, // contributorIds
                null, null, null, null, null, null, null, null, null,
                true, // includeApplicants
                null, null, null, null, null, null, null, null, null, null, null);
    }

    default Page<ContributorKpisReadEntity> findAll(BiContributorsQueryParams q) {
        final var sanitizedFromDate = q.getFromDate() == null ? null : parseZonedNullable(q.getFromDate()).truncatedTo(ChronoUnit.DAYS);
        final var sanitizedToDate = q.getToDate() == null ? null : parseZonedNullable(q.getToDate()).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        final var fromDateOfPreviousPeriod = sanitizedFromDate == null || sanitizedToDate == null ? null :
                sanitizedFromDate.minusSeconds(sanitizedToDate.toEpochSecond() - sanitizedFromDate.toEpochSecond());

        return findAll(
                sanitizedFromDate,
                sanitizedToDate,
                fromDateOfPreviousPeriod,
                sanitizedFromDate,
                q.getDataSource() == DataSourceEnum.ONLYDUST,
                q.getDataSourceIds() == null ? null : q.getDataSourceIds().toArray(UUID[]::new),
                q.getShowFilteredKpis(),
                q.getSearch(),
                q.getContributorIds() == null ? null : q.getContributorIds().toArray(Long[]::new),
                q.getContributedTo() == null ? null : q.getContributedTo().toArray(UUID[]::new),
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                q.getProjectSlugs() == null ? null : q.getProjectSlugs().toArray(String[]::new),
                q.getCategoryIds() == null ? null : q.getCategoryIds().toArray(UUID[]::new),
                q.getLanguageIds() == null ? null : q.getLanguageIds().toArray(UUID[]::new),
                q.getEcosystemIds() == null ? null : q.getEcosystemIds().toArray(UUID[]::new),
                q.getCountryCodes() == null ? null : q.getCountryCodes().stream().map(c -> Country.fromIso2(c).iso3Code()).toArray(String[]::new),
                q.getContributionStatuses() == null ? null : q.getContributionStatuses().stream().map(Enum::name).toArray(String[]::new),
                q.getIncludeApplicants(),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(ContributorsQueryParamsContributionCount::getGte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(ContributorsQueryParamsContributionCount::getEq).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(ContributorsQueryParamsContributionCount::getLte).orElse(null),
                q.getContributionCount() == null ? null : q.getContributionCount().getTypes().stream().map(Enum::name).toArray(String[]::new),
                q.getActivityStatuses() == null ? null : q.getActivityStatuses().stream().map(Enum::name).toArray(String[]::new),
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ContributorKpisReadRepository.getSortProperty(q.getSort())))
        );
    }

    default Page<ContributorKpisReadEntity> findAll(ApplicantsQueryParams q, List<UUID> labelProjectIds) {
        return findAll(
                null,
                null,
                null,
                null,
                q.getDataSource() == DataSourceEnum.ONLYDUST,
                null,
                q.getShowFilteredKpis(),
                q.getSearch(),
                q.getContributorIds() == null ? null : q.getContributorIds().toArray(Long[]::new),
                null,
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                labelProjectIds.toArray(UUID[]::new),
                q.getProjectSlugs() == null ? null : q.getProjectSlugs().toArray(String[]::new),
                q.getCategoryIds() == null ? null : q.getCategoryIds().toArray(UUID[]::new),
                q.getLanguageIds() == null ? null : q.getLanguageIds().toArray(UUID[]::new),
                q.getEcosystemIds() == null ? null : q.getEcosystemIds().toArray(UUID[]::new),
                q.getCountryCodes() == null ? null : q.getCountryCodes().stream().map(c -> Country.fromIso2(c).iso3Code()).toArray(String[]::new),
                q.getContributionStatuses() == null ? null : q.getContributionStatuses().stream().map(Enum::name).toArray(String[]::new),
                q.getIncludeApplicants(),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(ContributorsQueryParamsContributionCount::getGte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(ContributorsQueryParamsContributionCount::getEq).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(ContributorsQueryParamsContributionCount::getLte).orElse(null),
                q.getContributionCount() == null ? null : q.getContributionCount().getTypes().stream().map(Enum::name).toArray(String[]::new),
                null,
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ContributorKpisReadRepository.getSortProperty(q.getSort())))
        );
    }
}

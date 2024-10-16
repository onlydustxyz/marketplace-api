package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.bi.ContributorKpisReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.sanitizedDate;

public interface ContributorKpisReadRepository extends Repository<ContributorKpisReadEntity, Long> {
    ZonedDateTime DEFAULT_FROM_DATE = ZonedDateTime.parse("2007-10-20T05:24:19Z");

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
            
            FROM bi.select_contributors(:fromDate, :toDate, :dataSourceIds, :contributorIds, ROW(:contributedToContribGithubId, cast(:contributedToContribType as indexer_exp.contribution_type)),
                                        :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, cast(:contributionStatuses as indexer_exp.contribution_status[]), :search, :showFilteredKpis) d
                     LEFT JOIN (
                            select * from bi.select_contributors(:fromDatePreviousPeriod, :toDatePreviousPeriod, :dataSourceIds, :contributorIds, ROW(:contributedToContribGithubId, cast(:contributedToContribType as indexer_exp.contribution_type)),
                                                                 :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, cast(:contributionStatuses as indexer_exp.contribution_status[]), :search, :showFilteredKpis)
                         ) previous_period ON coalesce(:fromDatePreviousPeriod, :toDatePreviousPeriod) is not null and previous_period.contributor_id = d.contributor_id
            
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
                    FROM bi.select_contributors(:fromDate, :toDate, :dataSourceIds, :contributorIds, ROW(:contributedToContribGithubId, cast(:contributedToContribType as indexer_exp.contribution_type)),
                                                :projectIds, :projectSlugs, :categoryIds, :languageIds, :ecosystemIds, :countryCodes, cast(:contributionStatuses as indexer_exp.contribution_status[]), :search, :showFilteredKpis) d
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
    Page<ContributorKpisReadEntity> findAll(ZonedDateTime fromDate,
                                            ZonedDateTime toDate,
                                            ZonedDateTime fromDatePreviousPeriod,
                                            ZonedDateTime toDatePreviousPeriod,
                                            UUID[] dataSourceIds,
                                            @NonNull Boolean showFilteredKpis,
                                            String search,
                                            Long[] contributorIds,
                                            Long contributedToContribGithubId,
                                            ContributionType contributedToContribType,
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

    default Page<ContributorKpisReadEntity> findAll(BiContributorsQueryParams q) {
        final var sanitizedFromDate = sanitizedDate(q.getFromDate(), DEFAULT_FROM_DATE).truncatedTo(ChronoUnit.DAYS);
        final var sanitizedToDate = sanitizedDate(q.getToDate(), ZonedDateTime.now()).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        final var fromDateOfPreviousPeriod = sanitizedFromDate.minusSeconds(sanitizedToDate.toEpochSecond() - sanitizedFromDate.toEpochSecond());

        return findAll(
                sanitizedFromDate,
                sanitizedToDate,
                fromDateOfPreviousPeriod,
                sanitizedFromDate,
                q.getDataSourceIds() == null ? null : q.getDataSourceIds().toArray(UUID[]::new),
                q.getShowFilteredKpis(),
                q.getSearch(),
                q.getContributorIds() == null ? null : q.getContributorIds().toArray(Long[]::new),
                q.getContributedTo() == null ? null : q.getContributedTo().getGithubId(),
                q.getContributedTo() == null ? null : q.getContributedTo().getType(),
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                q.getProjectSlugs() == null ? null : q.getProjectSlugs().toArray(String[]::new),
                q.getCategoryIds() == null ? null : q.getCategoryIds().toArray(UUID[]::new),
                q.getLanguageIds() == null ? null : q.getLanguageIds().toArray(UUID[]::new),
                q.getEcosystemIds() == null ? null : q.getEcosystemIds().toArray(UUID[]::new),
                q.getCountryCodes() == null ? null : q.getCountryCodes().stream().map(c -> Country.fromIso2(c).iso3Code()).toArray(String[]::new),
                q.getContributionStatuses() == null ? null : q.getContributionStatuses().stream().map(Enum::name).toArray(String[]::new),
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
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ContributorKpisReadRepository.getSortProperty(q.getSort())))
        );
    }

    default Page<ContributorKpisReadEntity> findAll(ApplicantsQueryParams q) {
        return findAll(
                null,
                null,
                null,
                null,
                null,
                q.getShowFilteredKpis(),
                q.getSearch(),
                q.getContributorIds() == null ? null : q.getContributorIds().toArray(Long[]::new),
                null,
                null,
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                q.getProjectSlugs() == null ? null : q.getProjectSlugs().toArray(String[]::new),
                q.getCategoryIds() == null ? null : q.getCategoryIds().toArray(UUID[]::new),
                q.getLanguageIds() == null ? null : q.getLanguageIds().toArray(UUID[]::new),
                q.getEcosystemIds() == null ? null : q.getEcosystemIds().toArray(UUID[]::new),
                q.getCountryCodes() == null ? null : q.getCountryCodes().stream().map(c -> Country.fromIso2(c).iso3Code()).toArray(String[]::new),
                q.getContributionStatuses() == null ? null : q.getContributionStatuses().stream().map(Enum::name).toArray(String[]::new),
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
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ContributorKpisReadRepository.getSortProperty(q.getSort())))
        );
    }
}

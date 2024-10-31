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
    ZonedDateTime DEFAULT_FROM_DATE = ZonedDateTime.parse("2007-10-20T05:24:19Z");

    @Language("PostgreSQL")
    String SELECT = """
            SELECT * -- wrap the query in a subquery to let Hibernate paginate
            FROM (SELECT c.contributor_id                                                          as contributor_id,
                         c.contributor_login                                                       as contributor_login,
                         c.contributor_country                                                     as contributor_country,
                         c.contributor                                                             as contributor,
                         c.projects                                                                as projects,
                         c.categories                                                              as categories,
                         c.languages                                                               as languages,
                         c.ecosystems                                                              as ecosystems,
                         c.maintained_projects                                                     as maintained_projects,
                         c.first_project_name                                                      as first_project_name,
                         -- /// filtered & computed data /// --
                         (select jsonb_agg(jsonb_build_object('id', pcl.id, 'slug', pcl.slug, 'name', pcl.name))
                          from contributor_project_contributor_labels cpcl
                                   join project_contributor_labels pcl on pcl.id = cpcl.label_id
                                   join projects p on p.id = pcl.project_id
                          where cpcl.github_user_id = c.contributor_id
                            and (coalesce(:projectIds) is null or p.id = any (:projectIds))
                            and (coalesce(:labelProjectIds) is null or p.id = any (:labelProjectIds))
                            and (coalesce(:projectSlugs) is null or p.slug = any (:projectSlugs))) as project_contributor_labels,
                         coalesce(sum(rd.total_rewarded_usd_amount), 0)                            as total_rewarded_usd_amount,
                         coalesce(sum(rd.reward_count), 0)                                         as reward_count,
                         coalesce(sum(cd.completed_contribution_count), 0)                         as completed_contribution_count,
                         coalesce(sum(cd.completed_issue_count), 0)                                as completed_issue_count,
                         coalesce(sum(cd.completed_pr_count), 0)                                   as completed_pr_count,
                         coalesce(sum(cd.completed_code_review_count), 0)                          as completed_code_review_count,
                         coalesce(sum(cd.in_progress_issue_count), 0)                              as in_progress_issue_count,
                         coalesce(sum(ad.pending_application_count), 0)                            as pending_application_count,
                         coalesce(sum(rd.previous_period_total_rewarded_usd_amount), 0)            as previous_period_total_rewarded_usd_amount,
                         coalesce(sum(rd.previous_period_reward_count), 0)                         as previous_period_reward_count,
                         coalesce(sum(cd.previous_period_completed_contribution_count), 0)         as previous_period_completed_contribution_count,
                         coalesce(sum(cd.previous_period_completed_issue_count), 0)                as previous_period_completed_issue_count,
                         coalesce(sum(cd.previous_period_completed_pr_count), 0)                   as previous_period_completed_pr_count,
                         coalesce(sum(cd.previous_period_completed_code_review_count), 0)          as previous_period_completed_code_review_count,
                         coalesce(sum(cd.previous_period_in_progress_issue_count), 0)              as previous_period_in_progress_issue_count,
                         coalesce(sum(ad.previous_period_pending_application_count), 0)            as previous_period_pending_application_count,
                         activity_status.value                                                     as activity_status
            
                  FROM bi.p_contributor_global_data c
                           JOIN bi.p_contributor_reward_data crd ON crd.contributor_id = c.contributor_id
                           JOIN bi.p_contributor_application_data cad ON cad.contributor_id = c.contributor_id
            
                           LEFT JOIN (select cd.contributor_id                                                                                                                             as contributor_id,
                                             bool_or(cd.contribution_uuid in (:contributedTo))                                                                                             as contributed_to,
            
                                             count(cd.contribution_uuid) filter ( where cd.timestamp >= :fromDate )                                                                        as contribution_count,
                                             bool_or(cd.is_first_contribution_on_onlydust) filter ( where cd.timestamp >= :fromDate )                                                      as includes_first_contribution_on_onlydust,
                                             count(cd.contribution_uuid) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'COMPLETED' )                               as completed_contribution_count,
                                             count(cd.contribution_uuid) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'COMPLETED' and cd.project_id is not null ) as od_completed_contribution_count,
                                             coalesce(sum(cd.is_issue) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                             as completed_issue_count,
                                             coalesce(sum(cd.is_pr) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                                as completed_pr_count,
                                             coalesce(sum(cd.is_code_review) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                       as completed_code_review_count,
                                             coalesce(sum(cd.is_issue) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'IN_PROGRESS' ), 0)                           as in_progress_issue_count,
            
                                             count(cd.contribution_uuid) filter ( where cd.timestamp < :fromDate )                                                                         as previous_period_contribution_count,
                                             bool_or(cd.is_first_contribution_on_onlydust) filter ( where cd.timestamp < :fromDate )                                                       as previous_period_includes_first_contribution_on_onlydust,
                                             count(cd.contribution_uuid) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'COMPLETED' )                                as previous_period_completed_contribution_count,
                                             count(cd.contribution_uuid) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'COMPLETED' and cd.project_id is not null )  as previous_period_od_completed_contribution_count,
                                             coalesce(sum(cd.is_issue) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                              as previous_period_completed_issue_count,
                                             coalesce(sum(cd.is_pr) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                                 as previous_period_completed_pr_count,
                                             coalesce(sum(cd.is_code_review) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                        as previous_period_completed_code_review_count,
                                             coalesce(sum(cd.is_issue) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'IN_PROGRESS' ), 0)                            as previous_period_in_progress_issue_count
                                      from bi.p_per_contributor_contribution_data cd
                                      where (coalesce(:fromDatePreviousPeriod) is null or cd.timestamp >= :fromDatePreviousPeriod)
                                        and (coalesce(:toDate) is null or cd.timestamp < :toDate)
                                        and (:onlyDustContributionsOnly is false or cd.project_id is not null)
                                        and (coalesce(:dataSourceIds) is null or cd.project_id = any (:dataSourceIds) or
                                             cd.program_ids && :dataSourceIds or cd.ecosystem_ids && :dataSourceIds)
                                        and (coalesce(:contributionStatuses) is null or cd.contribution_status = any (cast(:contributionStatuses as indexer_exp.contribution_status[])))
                                        and (not :filteredKpis or coalesce(:projectIds) is null or cd.project_id = any (:projectIds))
                                        and (not :filteredKpis or coalesce(:projectSlugs) is null or cd.project_slug = any (:projectSlugs))
                                        and (not :filteredKpis or coalesce(:ecosystemIds) is null or cd.ecosystem_ids && :ecosystemIds)
                                        and (not :filteredKpis or coalesce(:categoryIds) is null or cd.project_category_ids && :categoryIds)
                                        and (not :filteredKpis or coalesce(:languageIds) is null or cd.language_ids && :languageIds)
                                      group by cd.contributor_id) cd on cd.contributor_id = c.contributor_id
            
                           LEFT JOIN (select rd.contributor_id,
                                             count(rd.reward_id) filter ( where rd.timestamp >= :fromDate )             as reward_count,
                                             coalesce(sum(rd.usd_amount) filter ( where rd.timestamp >= :fromDate ), 0) as total_rewarded_usd_amount,
            
                                             count(rd.reward_id) filter ( where rd.timestamp < :fromDate )              as previous_period_reward_count,
                                             coalesce(sum(rd.usd_amount) filter ( where rd.timestamp < :fromDate ), 0)  as previous_period_total_rewarded_usd_amount
                                      from bi.p_reward_data rd
                                      where (coalesce(:fromDatePreviousPeriod) is null or rd.timestamp >= :fromDatePreviousPeriod)
                                        and (coalesce(:toDate) is null or rd.timestamp < :toDate)
                                        and (coalesce(:dataSourceIds) is null or rd.project_id = any (:dataSourceIds) or
                                             rd.program_ids && :dataSourceIds or rd.ecosystem_ids && :dataSourceIds)
                                        and (not :filteredKpis or coalesce(:projectIds) is null or rd.project_id = any (:projectIds))
                                        and (not :filteredKpis or coalesce(:projectSlugs) is null or rd.project_slug = any (:projectSlugs))
                                        and (not :filteredKpis or coalesce(:ecosystemIds) is null or rd.ecosystem_ids && :ecosystemIds)
                                        and (not :filteredKpis or coalesce(:categoryIds) is null or rd.project_category_ids && :categoryIds)
                                        and (not :filteredKpis or coalesce(:languageIds) is null or rd.language_ids && :languageIds)
                                      group by rd.contributor_id) rd on rd.contributor_id = c.contributor_id
            
                           LEFT JOIN (select ad.contributor_id,
                                             count(ad.application_id) filter ( where ad.timestamp >= :fromDate and ad.status = 'PENDING' ) as pending_application_count,
                                             count(ad.application_id) filter ( where ad.timestamp < :fromDate and ad.status = 'PENDING' )  as previous_period_pending_application_count
                                      from bi.p_application_data ad
                                      where (coalesce(:fromDatePreviousPeriod) is null or ad.timestamp >= :fromDatePreviousPeriod)
                                        and (coalesce(:toDate) is null or ad.timestamp < :toDate)
                                        and (coalesce(:dataSourceIds) is null or ad.project_id = any (:dataSourceIds) or
                                             ad.program_ids && :dataSourceIds or ad.ecosystem_ids && :dataSourceIds)
                                        and (not :filteredKpis or coalesce(:projectIds) is null or ad.project_id = any (:projectIds))
                                        and (not :filteredKpis or coalesce(:projectSlugs) is null or ad.project_slug = any (:projectSlugs))
                                        and (not :filteredKpis or coalesce(:ecosystemIds) is null or ad.ecosystem_ids && :ecosystemIds)
                                        and (not :filteredKpis or coalesce(:categoryIds) is null or ad.project_category_ids && :categoryIds)
                                        and (not :filteredKpis or coalesce(:languageIds) is null or ad.language_ids && :languageIds)
                                      group by ad.contributor_id) ad on :includeApplicants and ad.contributor_id = c.contributor_id
            
                           LEFT JOIN LATERAL ( select cast(case
                                                               when coalesce(cd.previous_period_od_completed_contribution_count, 0) > 0 and
                                                                    coalesce(cd.od_completed_contribution_count, 0) > 0
                                                                   then 'ACTIVE'
                                                               when coalesce(cd.previous_period_od_completed_contribution_count, 0) > 0 and
                                                                    coalesce(cd.od_completed_contribution_count, 0) = 0
                                                                   then 'CHURNED'
                                                               when coalesce(cd.previous_period_od_completed_contribution_count, 0) = 0 and
                                                                    coalesce(cd.includes_first_contribution_on_onlydust, false) and
                                                                    coalesce(cd.od_completed_contribution_count, 0) > 0
                                                                   then 'NEW'
                                                               when coalesce(cd.previous_period_od_completed_contribution_count, 0) = 0 and
                                                                    not coalesce(cd.includes_first_contribution_on_onlydust, false) and
                                                                    coalesce(cd.od_completed_contribution_count, 0) > 0
                                                                   then 'REACTIVATED'
                                                               else 'INACTIVE' end as contributor_activity_status) as value) activity_status ON true
            
            
                  WHERE (coalesce(:dataSourceIds) is null or
                         c.contributed_on_project_ids && :dataSourceIds or
                         cad.applied_on_project_ids && :dataSourceIds or
                         c.program_ids && :dataSourceIds or
                         c.ecosystem_ids && :dataSourceIds)
                    and (coalesce(:contributorIds) is null or c.contributor_id = any (:contributorIds))
                    and (coalesce(:projectIds) is null or c.contributed_on_project_ids && :projectIds or (:includeApplicants and cad.applied_on_project_ids && :projectIds))
                    and (coalesce(:projectSlugs) is null or c.contributed_on_project_slugs && cast(:projectSlugs as text[]) or (:includeApplicants and cad.applied_on_project_slugs && cast(:projectSlugs as text[])))
                    and (coalesce(:ecosystemIds) is null or c.ecosystem_ids && :ecosystemIds)
                    and (coalesce(:categoryIds) is null or c.project_category_ids && :categoryIds)
                    and (coalesce(:languageIds) is null or c.language_ids && :languageIds)
                    and (coalesce(:countryCodes) is null or c.contributor_country = any (:countryCodes))
                    and (coalesce(:searchQuery) is null or c.search ilike '%' || :searchQuery || '%' or crd.search ilike '%' || :searchQuery || '%')
                    and (activity_status.value != 'INACTIVE' or cd.contribution_count > 0 or rd.reward_count > 0 or ad.pending_application_count > 0)
                    and (coalesce(:totalRewardedUsdAmountMin) is null or rd.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
                    and (coalesce(:totalRewardedUsdAmountEq) is null or rd.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
                    and (coalesce(:totalRewardedUsdAmountMax) is null or rd.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
                    and (coalesce(:rewardCountMin) is null or rd.reward_count >= :rewardCountMin)
                    and (coalesce(:rewardCountEq) is null or rd.reward_count = :rewardCountEq)
                    and (coalesce(:rewardCountMax) is null or rd.reward_count <= :rewardCountMax)
                    and (coalesce(:completedContributionCountMin) is null or
                         (
                             case when 'ISSUE' in (:contributionTypes) then cd.completed_issue_count else 0 end +
                             case when 'PULL_REQUEST' in (:contributionTypes) then cd.completed_pr_count else 0 end +
                             case when 'CODE_REVIEW' in (:contributionTypes) then cd.completed_code_review_count else 0 end
                             ) >= :completedContributionCountMin)
                    and (coalesce(:completedContributionCountEq) is null or
                         (
                             case when 'ISSUE' in (:contributionTypes) then cd.completed_issue_count else 0 end +
                             case when 'PULL_REQUEST' in (:contributionTypes) then cd.completed_pr_count else 0 end +
                             case when 'CODE_REVIEW' in (:contributionTypes) then cd.completed_code_review_count else 0 end
                             ) = :completedContributionCountEq)
                    and (coalesce(:completedContributionCountMax) is null or
                         (
                             case when 'ISSUE' in (:contributionTypes) then cd.completed_issue_count else 0 end +
                             case when 'PULL_REQUEST' in (:contributionTypes) then cd.completed_pr_count else 0 end +
                             case when 'CODE_REVIEW' in (:contributionTypes) then cd.completed_code_review_count else 0 end
                             ) <= :completedContributionCountMax)
                    and (coalesce(:contributedTo) is null or cd.contributed_to is true)
                    and (coalesce(:activityStatuses) is null or activity_status.value = any (cast(:activityStatuses as contributor_activity_status[])))
                  GROUP BY c.contributor_id,
                           c.contributor_login,
                           c.contributor_country,
                           c.contributor,
                           c.projects,
                           c.categories,
                           c.languages,
                           c.ecosystems,
                           c.maintained_projects,
                           c.first_project_name,
                           activity_status.value) d
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
                    
                    FROM bi.p_contributor_global_data c
                             JOIN bi.p_contributor_reward_data crd ON crd.contributor_id = c.contributor_id
                             JOIN bi.p_contributor_application_data cad ON cad.contributor_id = c.contributor_id
                    
                             LEFT JOIN (select cd.contributor_id                                                                                                                             as contributor_id,
                                               bool_or(cd.contribution_uuid in (:contributedTo))                                                                                             as contributed_to,
                    
                                               count(cd.contribution_uuid) filter ( where cd.timestamp >= :fromDate )                                                                        as contribution_count,
                                               bool_or(cd.is_first_contribution_on_onlydust) filter ( where cd.timestamp >= :fromDate )                                                      as includes_first_contribution_on_onlydust,
                                               count(cd.contribution_uuid) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'COMPLETED' )                               as completed_contribution_count,
                                               count(cd.contribution_uuid) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'COMPLETED' and cd.project_id is not null ) as od_completed_contribution_count,
                                               coalesce(sum(cd.is_issue) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                             as completed_issue_count,
                                               coalesce(sum(cd.is_pr) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                                as completed_pr_count,
                                               coalesce(sum(cd.is_code_review) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                       as completed_code_review_count,
                                               coalesce(sum(cd.is_issue) filter ( where cd.timestamp >= :fromDate and cd.contribution_status = 'IN_PROGRESS' ), 0)                           as in_progress_issue_count,
                    
                                               count(cd.contribution_uuid) filter ( where cd.timestamp < :fromDate )                                                                         as previous_period_contribution_count,
                                               bool_or(cd.is_first_contribution_on_onlydust) filter ( where cd.timestamp < :fromDate )                                                       as previous_period_includes_first_contribution_on_onlydust,
                                               count(cd.contribution_uuid) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'COMPLETED' )                                as previous_period_completed_contribution_count,
                                               count(cd.contribution_uuid) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'COMPLETED' and cd.project_id is not null )  as previous_period_od_completed_contribution_count,
                                               coalesce(sum(cd.is_issue) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                              as previous_period_completed_issue_count,
                                               coalesce(sum(cd.is_pr) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                                 as previous_period_completed_pr_count,
                                               coalesce(sum(cd.is_code_review) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'COMPLETED' ), 0)                        as previous_period_completed_code_review_count,
                                               coalesce(sum(cd.is_issue) filter ( where cd.timestamp < :fromDate and cd.contribution_status = 'IN_PROGRESS' ), 0)                            as previous_period_in_progress_issue_count
                                        from bi.p_per_contributor_contribution_data cd
                                        where (coalesce(:fromDatePreviousPeriod) is null or cd.timestamp >= :fromDatePreviousPeriod)
                                          and (coalesce(:toDate) is null or cd.timestamp < :toDate)
                                          and (:onlyDustContributionsOnly is false or cd.project_id is not null)
                                          and (coalesce(:dataSourceIds) is null or cd.project_id = any (:dataSourceIds) or
                                               cd.program_ids && :dataSourceIds or cd.ecosystem_ids && :dataSourceIds)
                                          and (coalesce(:contributionStatuses) is null or cd.contribution_status = any (cast(:contributionStatuses as indexer_exp.contribution_status[])))
                                          and (not :filteredKpis or coalesce(:projectIds) is null or cd.project_id = any (:projectIds))
                                          and (not :filteredKpis or coalesce(:projectSlugs) is null or cd.project_slug = any (:projectSlugs))
                                          and (not :filteredKpis or coalesce(:ecosystemIds) is null or cd.ecosystem_ids && :ecosystemIds)
                                          and (not :filteredKpis or coalesce(:categoryIds) is null or cd.project_category_ids && :categoryIds)
                                          and (not :filteredKpis or coalesce(:languageIds) is null or cd.language_ids && :languageIds)
                                        group by cd.contributor_id) cd on cd.contributor_id = c.contributor_id
                    
                             LEFT JOIN (select rd.contributor_id,
                                               count(rd.reward_id) filter ( where rd.timestamp >= :fromDate )             as reward_count,
                                               coalesce(sum(rd.usd_amount) filter ( where rd.timestamp >= :fromDate ), 0) as total_rewarded_usd_amount,
                    
                                               count(rd.reward_id) filter ( where rd.timestamp < :fromDate )              as previous_period_reward_count,
                                               coalesce(sum(rd.usd_amount) filter ( where rd.timestamp < :fromDate ), 0)  as previous_period_total_rewarded_usd_amount
                                        from bi.p_reward_data rd
                                        where (coalesce(:fromDatePreviousPeriod) is null or rd.timestamp >= :fromDatePreviousPeriod)
                                          and (coalesce(:toDate) is null or rd.timestamp < :toDate)
                                          and (coalesce(:dataSourceIds) is null or rd.project_id = any (:dataSourceIds) or
                                               rd.program_ids && :dataSourceIds or rd.ecosystem_ids && :dataSourceIds)
                                          and (not :filteredKpis or coalesce(:projectIds) is null or rd.project_id = any (:projectIds))
                                          and (not :filteredKpis or coalesce(:projectSlugs) is null or rd.project_slug = any (:projectSlugs))
                                          and (not :filteredKpis or coalesce(:ecosystemIds) is null or rd.ecosystem_ids && :ecosystemIds)
                                          and (not :filteredKpis or coalesce(:categoryIds) is null or rd.project_category_ids && :categoryIds)
                                          and (not :filteredKpis or coalesce(:languageIds) is null or rd.language_ids && :languageIds)
                                        group by rd.contributor_id) rd on rd.contributor_id = c.contributor_id
                    
                             LEFT JOIN (select ad.contributor_id,
                                               count(ad.application_id) filter ( where ad.timestamp >= :fromDate and ad.status = 'PENDING' ) as pending_application_count,
                                               count(ad.application_id) filter ( where ad.timestamp < :fromDate and ad.status = 'PENDING' )  as previous_period_pending_application_count
                                        from bi.p_application_data ad
                                        where (coalesce(:fromDatePreviousPeriod) is null or ad.timestamp >= :fromDatePreviousPeriod)
                                          and (coalesce(:toDate) is null or ad.timestamp < :toDate)
                                          and (coalesce(:dataSourceIds) is null or ad.project_id = any (:dataSourceIds) or
                                               ad.program_ids && :dataSourceIds or ad.ecosystem_ids && :dataSourceIds)
                                          and (not :filteredKpis or coalesce(:projectIds) is null or ad.project_id = any (:projectIds))
                                          and (not :filteredKpis or coalesce(:projectSlugs) is null or ad.project_slug = any (:projectSlugs))
                                          and (not :filteredKpis or coalesce(:ecosystemIds) is null or ad.ecosystem_ids && :ecosystemIds)
                                          and (not :filteredKpis or coalesce(:categoryIds) is null or ad.project_category_ids && :categoryIds)
                                          and (not :filteredKpis or coalesce(:languageIds) is null or ad.language_ids && :languageIds)
                                        group by ad.contributor_id) ad on :includeApplicants and ad.contributor_id = c.contributor_id
                    
                             LEFT JOIN LATERAL ( select cast(case
                                                                 when coalesce(cd.previous_period_od_completed_contribution_count, 0) > 0 and
                                                                      coalesce(cd.od_completed_contribution_count, 0) > 0
                                                                     then 'ACTIVE'
                                                                 when coalesce(cd.previous_period_od_completed_contribution_count, 0) > 0 and
                                                                      coalesce(cd.od_completed_contribution_count, 0) = 0
                                                                     then 'CHURNED'
                                                                 when coalesce(cd.previous_period_od_completed_contribution_count, 0) = 0 and
                                                                      coalesce(cd.includes_first_contribution_on_onlydust, false) and
                                                                      coalesce(cd.od_completed_contribution_count, 0) > 0
                                                                     then 'NEW'
                                                                 when coalesce(cd.previous_period_od_completed_contribution_count, 0) = 0 and
                                                                      not coalesce(cd.includes_first_contribution_on_onlydust, false) and
                                                                      coalesce(cd.od_completed_contribution_count, 0) > 0
                                                                     then 'REACTIVATED'
                                                                 else 'INACTIVE' end as contributor_activity_status) as value) activity_status ON true
                    
                    
                    WHERE (coalesce(:dataSourceIds) is null or
                           c.contributed_on_project_ids && :dataSourceIds or
                           cad.applied_on_project_ids && :dataSourceIds or
                           c.program_ids && :dataSourceIds or
                           c.ecosystem_ids && :dataSourceIds)
                      and (coalesce(:contributorIds) is null or c.contributor_id = any (:contributorIds))
                      and (coalesce(:projectIds) is null or c.contributed_on_project_ids && :projectIds or (:includeApplicants and cad.applied_on_project_ids && :projectIds))
                      and (coalesce(:projectSlugs) is null or c.contributed_on_project_slugs && cast(:projectSlugs as text[]) or (:includeApplicants and cad.applied_on_project_slugs && cast(:projectSlugs as text[])))
                      and (coalesce(:ecosystemIds) is null or c.ecosystem_ids && :ecosystemIds)
                      and (coalesce(:categoryIds) is null or c.project_category_ids && :categoryIds)
                      and (coalesce(:languageIds) is null or c.language_ids && :languageIds)
                      and (coalesce(:countryCodes) is null or c.contributor_country = any (:countryCodes))
                      and (coalesce(:searchQuery) is null or c.search ilike '%' || :searchQuery || '%' or crd.search ilike '%' || :searchQuery || '%')
                      and (activity_status.value != 'INACTIVE' or cd.contribution_count > 0 or rd.reward_count > 0 or ad.pending_application_count > 0)
                      and (coalesce(:totalRewardedUsdAmountMin) is null or rd.total_rewarded_usd_amount >= :totalRewardedUsdAmountMin)
                      and (coalesce(:totalRewardedUsdAmountEq) is null or rd.total_rewarded_usd_amount = :totalRewardedUsdAmountEq)
                      and (coalesce(:totalRewardedUsdAmountMax) is null or rd.total_rewarded_usd_amount <= :totalRewardedUsdAmountMax)
                      and (coalesce(:rewardCountMin) is null or rd.reward_count >= :rewardCountMin)
                      and (coalesce(:rewardCountEq) is null or rd.reward_count = :rewardCountEq)
                      and (coalesce(:rewardCountMax) is null or rd.reward_count <= :rewardCountMax)
                      and (coalesce(:completedContributionCountMin) is null or
                           (
                               case when 'ISSUE' in (:contributionTypes) then cd.completed_issue_count else 0 end +
                               case when 'PULL_REQUEST' in (:contributionTypes) then cd.completed_pr_count else 0 end +
                               case when 'CODE_REVIEW' in (:contributionTypes) then cd.completed_code_review_count else 0 end
                               ) >= :completedContributionCountMin)
                      and (coalesce(:completedContributionCountEq) is null or
                           (
                               case when 'ISSUE' in (:contributionTypes) then cd.completed_issue_count else 0 end +
                               case when 'PULL_REQUEST' in (:contributionTypes) then cd.completed_pr_count else 0 end +
                               case when 'CODE_REVIEW' in (:contributionTypes) then cd.completed_code_review_count else 0 end
                               ) = :completedContributionCountEq)
                      and (coalesce(:completedContributionCountMax) is null or
                           (
                               case when 'ISSUE' in (:contributionTypes) then cd.completed_issue_count else 0 end +
                               case when 'PULL_REQUEST' in (:contributionTypes) then cd.completed_pr_count else 0 end +
                               case when 'CODE_REVIEW' in (:contributionTypes) then cd.completed_code_review_count else 0 end
                               ) <= :completedContributionCountMax)
                      and (coalesce(:contributedTo) is null or cd.contributed_to is true)
                      and (coalesce(:activityStatuses) is null or activity_status.value = any (cast(:activityStatuses as contributor_activity_status[])))
                    GROUP BY c.contributor_id,
                             c.contributor_login,
                             c.contributor_country,
                             c.contributor,
                             c.projects,
                             c.categories,
                             c.languages,
                             c.ecosystems,
                             c.maintained_projects,
                             c.first_project_name,
                             activity_status.value
                    """,
            nativeQuery = true)
    Page<ContributorKpisReadEntity> findAll(ZonedDateTime fromDate,
                                            ZonedDateTime toDate,
                                            ZonedDateTime fromDatePreviousPeriod,
                                            @NonNull Boolean onlyDustContributionsOnly,
                                            UUID[] dataSourceIds,
                                            @NonNull Boolean filteredKpis,
                                            String searchQuery,
                                            Long[] contributorIds,
                                            List<UUID> contributedTo,
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
                                            List<String> contributionTypes,
                                            String[] activityStatuses,
                                            Pageable pageable);

    @Query(value = SELECT + " where d.contributor_id = :contributorId", nativeQuery = true)
    Optional<ContributorKpisReadEntity> findById(Long contributorId,
                                                 ZonedDateTime fromDate,
                                                 ZonedDateTime toDate,
                                                 ZonedDateTime fromDatePreviousPeriod,
                                                 @NonNull Boolean onlyDustContributionsOnly,
                                                 UUID[] dataSourceIds,
                                                 @NonNull Boolean filteredKpis,
                                                 String searchQuery,
                                                 Long[] contributorIds,
                                                 List<UUID> contributedTo,
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
                                                 List<String> contributionTypes,
                                                 String[] activityStatuses);

    default Optional<ContributorKpisReadEntity> findById(Long contributorId) {
        return findById(contributorId, DEFAULT_FROM_DATE, ZonedDateTime.now(), DEFAULT_FROM_DATE,
                false, // onlyDustContributionsOnly
                null,
                false, // filteredKpis
                null,
                new Long[]{contributorId}, // contributorIds
                null, null, null, null, null, null, null, null, null,
                true, // includeApplicants
                null, null, null, null, null, null, null, null, null, null, null);
    }

    default Page<ContributorKpisReadEntity> findAll(BiContributorsQueryParams q) {
        final var sanitizedFromDate = q.getFromDate() == null ? DEFAULT_FROM_DATE : parseZonedNullable(q.getFromDate()).truncatedTo(ChronoUnit.DAYS);
        final var sanitizedToDate = q.getToDate() == null ? ZonedDateTime.now() : parseZonedNullable(q.getToDate()).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        final var fromDateOfPreviousPeriod = q.getFromDate() == null ? DEFAULT_FROM_DATE :
                sanitizedFromDate.minusSeconds(sanitizedToDate.toEpochSecond() - sanitizedFromDate.toEpochSecond());

        return findAll(
                sanitizedFromDate,
                sanitizedToDate,
                fromDateOfPreviousPeriod,
                q.getDataSource() == DataSourceEnum.ONLYDUST,
                q.getDataSourceIds() == null ? null : q.getDataSourceIds().toArray(UUID[]::new),
                q.getShowFilteredKpis(),
                q.getSearch(),
                q.getContributorIds() == null ? null : q.getContributorIds().toArray(Long[]::new),
                q.getContributedTo(),
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
                q.getContributionCount() == null ? null : q.getContributionCount().getTypes().stream().map(Enum::name).toList(),
                q.getActivityStatuses() == null ? null : q.getActivityStatuses().stream().map(Enum::name).toArray(String[]::new),
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ContributorKpisReadRepository.getSortProperty(q.getSort())))
        );
    }

    default Page<ContributorKpisReadEntity> findAll(ApplicantsQueryParams q, List<UUID> labelProjectIds) {
        return findAll(
                DEFAULT_FROM_DATE,
                ZonedDateTime.now(),
                DEFAULT_FROM_DATE,
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
                q.getContributionCount() == null ? null : q.getContributionCount().getTypes().stream().map(Enum::name).toList(),
                null,
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ContributorKpisReadRepository.getSortProperty(q.getSort())))
        );
    }
}

package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.bi.BOContributorBiReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface BOContributorBiReadRepository extends Repository<BOContributorBiReadEntity, Long> {

    @Query(value = """
            SELECT c.contributor_id                                                      as contributor_id,
                   c.contributor_login                                                   as contributor_login,
                   tlgm.contact                                                          as telegram,
                   (select count(*) from jsonb_array_elements(c.maintained_projects) mp) as maintained_project_count,
                   c.contributor                                                         as contributor,
                   jsonb_build_object(
                           'languages', (select jsonb_agg(l -> 'name') from jsonb_array_elements(c.languages) l),
                           'ecosystems', (select jsonb_agg(e -> 'name') from jsonb_array_elements(c.ecosystems) e),
                           'categories', (select jsonb_agg(cat -> 'name') from jsonb_array_elements(c.categories) cat),
                           'completedContributionCount', sum(coalesce(cd.completed_contribution_count, 0)),
                           'completedIssueCount', sum(coalesce(cd.completed_issue_count, 0)),
                           'completedPrCount', sum(coalesce(cd.completed_pr_count, 0)),
                           'completedCodeReviewCount', sum(coalesce(cd.completed_code_review_count, 0)),
                           'odCompletedContributionCount', sum(coalesce(cd.od_completed_contribution_count, 0)),
                           'odCompletedIssueCount', sum(coalesce(cd.od_completed_issue_count, 0)),
                           'odCompletedPrCount', sum(coalesce(cd.od_completed_pr_count, 0)),
                           'odCompletedCodeReviewCount', sum(coalesce(cd.od_completed_code_review_count, 0)),
                           'inProgressIssueCount', sum(coalesce(cd.in_progress_issue_count, 0)),
                           'odInProgressIssueCount', sum(coalesce(cd.od_in_progress_issue_count, 0)),
                           'last1MonthCompletedPrCount', sum(coalesce(cd.last_1month_completed_pr_count, 0)),
                           'last1MonthOdCompletedPrCount', sum(coalesce(cd.last_1month_od_completed_pr_count, 0)),
                           'last3MonthCompletedPrCount', sum(coalesce(cd.last_3month_completed_pr_count, 0)),
                           'last3MonthOdCompletedPrCount', sum(coalesce(cd.last_3month_od_completed_pr_count, 0)),
                           'lastContributionDate', max(cd.last_contribution_date),
                           'lastOdContributionDate', max(cd.last_od_contribution_date),
                           'lastPrDate', max(cd.last_pr_date),
                           'lastOdPrDate', max(cd.last_od_pr_date),
                           'applicationsPending', sum(coalesce(ad.pending_application_count, 0)),
                           'totalApplications', sum(coalesce(ad.application_count, 0)),
                           'rewardCount', sum(coalesce(rd.reward_count, 0)),
                           'rewardedUsdTotal', sum(coalesce(rd.total_rewarded_usd_amount, 0))
                   )                                                                     as global_data,
                   jsonb_agg(jsonb_build_object('projectId', p.project_id,
                                                'projectName', p.project_name,
                                                'projectSlug', p.project_slug,
                                                'languages', (select jsonb_agg(l -> 'name') from jsonb_array_elements(p.languages) l),
                                                'ecosystems', (select jsonb_agg(e -> 'name') from jsonb_array_elements(p.ecosystems) e),
                                                'categories', (select jsonb_agg(cat -> 'name') from jsonb_array_elements(p.categories) cat),
                                                'completedContributionCount', coalesce(cd.completed_contribution_count, 0),
                                                'completedIssueCount', coalesce(cd.completed_issue_count, 0),
                                                'completedPrCount', coalesce(cd.completed_pr_count, 0),
                                                'completedCodeReviewCount', coalesce(cd.completed_code_review_count, 0),
                                                'odCompletedContributionCount', coalesce(cd.od_completed_contribution_count, 0),
                                                'odCompletedIssueCount', coalesce(cd.od_completed_issue_count, 0),
                                                'odCompletedPrCount', coalesce(cd.od_completed_pr_count, 0),
                                                'odCompletedCodeReviewCount', coalesce(cd.od_completed_code_review_count, 0),
                                                'inProgressIssueCount', coalesce(cd.in_progress_issue_count, 0),
                                                'odInProgressIssueCount', coalesce(cd.od_in_progress_issue_count, 0),
                                                'last1MonthCompletedPrCount', coalesce(cd.last_1month_completed_pr_count, 0),
                                                'last1MonthOdCompletedPrCount', coalesce(cd.last_1month_od_completed_pr_count, 0),
                                                'last3MonthCompletedPrCount', coalesce(cd.last_3month_completed_pr_count, 0),
                                                'last3MonthOdCompletedPrCount', coalesce(cd.last_3month_od_completed_pr_count, 0),
                                                'lastContributionDate', cd.last_contribution_date,
                                                'lastOdContributionDate', cd.last_od_contribution_date,
                                                'lastPrDate', cd.last_pr_date,
                                                'lastOdPrDate', cd.last_od_pr_date,
                                                'applicationsPending', coalesce(ad.pending_application_count, 0),
                                                'totalApplications', coalesce(ad.application_count, 0),
                                                'rewardCount', coalesce(rd.reward_count, 0),
                                                'rewardedUsdTotal', coalesce(rd.total_rewarded_usd_amount, 0)
                             ))                                                          as per_project_data
            
            FROM bi.p_contributor_global_data c
                     JOIN bi.p_contributor_reward_data crd ON crd.contributor_id = c.contributor_id
                     JOIN bi.p_contributor_application_data cad ON cad.contributor_id = c.contributor_id
                     LEFT JOIN contact_informations tlgm ON tlgm.user_id = c.contributor_user_id and tlgm.channel = 'TELEGRAM'
            
                     LEFT JOIN (select cd.contributor_id                                                                                                                                             as contributor_id,
                                       cd.project_id                                                                                                                                                 as project_id,
            
                                       count(cd.contribution_uuid) filter ( where cd.contribution_status = 'COMPLETED' )                                                                             as completed_contribution_count,
                                       coalesce(sum(cd.is_issue) filter ( where cd.contribution_status = 'COMPLETED' ), 0)                                                                           as completed_issue_count,
                                       coalesce(sum(cd.is_pr) filter ( where cd.contribution_status = 'COMPLETED' ), 0)                                                                              as completed_pr_count,
                                       coalesce(sum(cd.is_code_review) filter ( where cd.contribution_status = 'COMPLETED' ), 0)                                                                     as completed_code_review_count,
                                       count(cd.contribution_uuid) filter ( where cd.contribution_status = 'COMPLETED' and cd.project_id is not null )                                               as od_completed_contribution_count,
                                       coalesce(sum(cd.is_issue) filter ( where cd.contribution_status = 'COMPLETED' and cd.project_id is not null ), 0)                                             as od_completed_issue_count,
                                       coalesce(sum(cd.is_pr) filter ( where cd.contribution_status = 'COMPLETED' and cd.project_id is not null ), 0)                                                as od_completed_pr_count,
                                       coalesce(sum(cd.is_code_review) filter ( where cd.contribution_status = 'COMPLETED' and cd.project_id is not null ), 0)                                       as od_completed_code_review_count,
            
                                       coalesce(sum(cd.is_issue) filter ( where cd.contribution_status = 'IN_PROGRESS' ), 0)                                                                         as in_progress_issue_count,
                                       coalesce(sum(cd.is_issue) filter ( where cd.contribution_status = 'IN_PROGRESS' and cd.project_id is not null ), 0)                                           as od_in_progress_issue_count,
            
            
                                       coalesce(sum(cd.is_pr) filter ( where cd.contribution_status = 'COMPLETED' and cd.timestamp > now() - interval '30 days' ), 0)                                as last_1month_completed_pr_count,
                                       coalesce(sum(cd.is_pr) filter ( where cd.contribution_status = 'COMPLETED' and cd.timestamp > now() - interval '30 days' and cd.project_id is not null ), 0)  as last_1month_od_completed_pr_count,
            
                                       coalesce(sum(cd.is_pr) filter ( where cd.contribution_status = 'COMPLETED' and cd.timestamp > now() - interval '3 months' ), 0)                               as last_3month_completed_pr_count,
                                       coalesce(sum(cd.is_pr) filter ( where cd.contribution_status = 'COMPLETED' and cd.timestamp > now() - interval '3 months' and cd.project_id is not null ), 0) as last_3month_od_completed_pr_count,
            
                                       max(cd.timestamp) filter ( where cd.contribution_status = 'COMPLETED' )                                                                                       as last_contribution_date,
                                       max(cd.timestamp) filter ( where cd.contribution_status = 'COMPLETED' and cd.project_id is not null )                                                         as last_od_contribution_date,
                                       max(cd.timestamp) filter ( where cd.contribution_status = 'COMPLETED' and cd.contribution_type = 'PULL_REQUEST')                                              as last_pr_date,
                                       max(cd.timestamp) filter ( where cd.contribution_status = 'COMPLETED' and cd.contribution_type = 'PULL_REQUEST' and cd.project_id is not null )               as last_od_pr_date
            
                                from bi.p_per_contributor_contribution_data cd
                                group by cd.contributor_id, cd.project_id) cd on cd.contributor_id = c.contributor_id
            
                     LEFT JOIN (select rd.contributor_id,
                                       rd.project_id,
                                       count(rd.reward_id)             as reward_count,
                                       coalesce(sum(rd.usd_amount), 0) as total_rewarded_usd_amount
                                from bi.p_reward_data rd
                                group by rd.contributor_id, rd.project_id) rd on rd.contributor_id = c.contributor_id and (cd.project_id is null or rd.project_id = cd.project_id)
            
                     LEFT JOIN (select ad.contributor_id,
                                       ad.project_id,
                                       count(ad.application_id)                                        as application_count,
                                       count(ad.application_id) filter ( where ad.status = 'PENDING' ) as pending_application_count
                                from bi.p_application_data ad
                                group by ad.contributor_id, ad.project_id) ad on ad.contributor_id = c.contributor_id and (coalesce(cd.project_id, rd.project_id) is null or ad.project_id = coalesce(cd.project_id, rd.project_id))
            
                     LEFT JOIN bi.p_project_global_data p on p.project_id = coalesce(cd.project_id, rd.project_id, ad.project_id)
            
            WHERE c.contributor_login in (:contributorLogins)
            GROUP BY c.contributor_id, tlgm.contact
            """,
            nativeQuery = true)
    List<BOContributorBiReadEntity> findAll(List<String> contributorLogins);

}

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
                           'completedContributionCount', sum(coalesce(ppd.completed_contribution_count, 0)),
                           'completedIssueCount', sum(coalesce(ppd.completed_issue_count, 0)),
                           'completedPrCount', sum(coalesce(ppd.completed_pr_count, 0)),
                           'completedCodeReviewCount', sum(coalesce(ppd.completed_code_review_count, 0)),
                           'odCompletedContributionCount', sum(coalesce(ppd.od_completed_contribution_count, 0)),
                           'odCompletedIssueCount', sum(coalesce(ppd.od_completed_issue_count, 0)),
                           'odCompletedPrCount', sum(coalesce(ppd.od_completed_pr_count, 0)),
                           'odCompletedCodeReviewCount', sum(coalesce(ppd.od_completed_code_review_count, 0)),
                           'inProgressIssueCount', sum(coalesce(ppd.in_progress_issue_count, 0)),
                           'odInProgressIssueCount', sum(coalesce(ppd.od_in_progress_issue_count, 0)),
                           'last1MonthCompletedPrCount', sum(coalesce(ppd.last_1month_completed_pr_count, 0)),
                           'last1MonthOdCompletedPrCount', sum(coalesce(ppd.last_1month_od_completed_pr_count, 0)),
                           'last3MonthCompletedPrCount', sum(coalesce(ppd.last_3month_completed_pr_count, 0)),
                           'last3MonthOdCompletedPrCount', sum(coalesce(ppd.last_3month_od_completed_pr_count, 0)),
                           'lastContributionDate', max(ppd.last_contribution_date),
                           'lastOdContributionDate', max(ppd.last_od_contribution_date),
                           'lastPrDate', max(ppd.last_pr_date),
                           'lastOdPrDate', max(ppd.last_od_pr_date),
                           'applicationsPending', sum(coalesce(ppd.pending_application_count, 0)),
                           'totalApplications', sum(coalesce(ppd.application_count, 0)),
                           'rewardCount', sum(coalesce(ppd.reward_count, 0)),
                           'rewardedUsdTotal', sum(coalesce(ppd.total_rewarded_usd_amount, 0))
                   )                                                                     as global_data,
                   jsonb_agg(jsonb_build_object('projectId', p.project_id,
                                                'projectName', p.project_name,
                                                'projectSlug', p.project_slug,
                                                'languages', (select jsonb_agg(l -> 'name') from jsonb_array_elements(p.languages) l),
                                                'ecosystems', (select jsonb_agg(e -> 'name') from jsonb_array_elements(p.ecosystems) e),
                                                'categories', (select jsonb_agg(cat -> 'name') from jsonb_array_elements(p.categories) cat),
                                                'completedContributionCount', coalesce(ppd.completed_contribution_count, 0),
                                                'completedIssueCount', coalesce(ppd.completed_issue_count, 0),
                                                'completedPrCount', coalesce(ppd.completed_pr_count, 0),
                                                'completedCodeReviewCount', coalesce(ppd.completed_code_review_count, 0),
                                                'odCompletedContributionCount', coalesce(ppd.od_completed_contribution_count, 0),
                                                'odCompletedIssueCount', coalesce(ppd.od_completed_issue_count, 0),
                                                'odCompletedPrCount', coalesce(ppd.od_completed_pr_count, 0),
                                                'odCompletedCodeReviewCount', coalesce(ppd.od_completed_code_review_count, 0),
                                                'inProgressIssueCount', coalesce(ppd.in_progress_issue_count, 0),
                                                'odInProgressIssueCount', coalesce(ppd.od_in_progress_issue_count, 0),
                                                'last1MonthCompletedPrCount', coalesce(ppd.last_1month_completed_pr_count, 0),
                                                'last1MonthOdCompletedPrCount', coalesce(ppd.last_1month_od_completed_pr_count, 0),
                                                'last3MonthCompletedPrCount', coalesce(ppd.last_3month_completed_pr_count, 0),
                                                'last3MonthOdCompletedPrCount', coalesce(ppd.last_3month_od_completed_pr_count, 0),
                                                'lastContributionDate', ppd.last_contribution_date,
                                                'lastOdContributionDate', ppd.last_od_contribution_date,
                                                'lastPrDate', ppd.last_pr_date,
                                                'lastOdPrDate', ppd.last_od_pr_date,
                                                'applicationsPending', coalesce(ppd.pending_application_count, 0),
                                                'totalApplications', coalesce(ppd.application_count, 0),
                                                'rewardCount', coalesce(ppd.reward_count, 0),
                                                'rewardedUsdTotal', coalesce(ppd.total_rewarded_usd_amount, 0)
                             ))                                                          as per_project_data
            
            FROM bi.p_contributor_global_data c
                     LEFT JOIN contact_informations tlgm ON tlgm.user_id = c.contributor_user_id and tlgm.channel = 'TELEGRAM'
            
                     LEFT JOIN LATERAL (select coalesce(cd.contributor_id, rd.contributor_id, ad.contributor_id)                                                                                             as contributor_id,
                                               coalesce(cd.project_id, rd.project_id, ad.project_id)                                                                                                         as project_id,
            
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
                                               max(cd.timestamp) filter ( where cd.contribution_status = 'COMPLETED' and cd.contribution_type = 'PULL_REQUEST' and cd.project_id is not null )               as last_od_pr_date,
            
                                               rd.reward_count                                                                                                                                               as reward_count,
                                               rd.total_rewarded_usd_amount                                                                                                                                  as total_rewarded_usd_amount,
            
                                               ad.application_count                                                                                                                                          as application_count,
                                               ad.pending_application_count                                                                                                                                  as pending_application_count
            
                                        from bi.p_per_contributor_contribution_data cd
                                                 full join (select rd.contributor_id,
                                                                   rd.project_id,
                                                                   count(rd.reward_id)             as reward_count,
                                                                   coalesce(sum(rd.usd_amount), 0) as total_rewarded_usd_amount
                                                            from bi.p_reward_data rd
                                                            where rd.contributor_id = c.contributor_id
                                                            group by rd.contributor_id, rd.project_id) rd on rd.contributor_id = cd.contributor_id and rd.project_id = cd.project_id
            
                                                 full join (select ad.contributor_id,
                                                                   ad.project_id,
                                                                   count(ad.application_id)                                        as application_count,
                                                                   count(ad.application_id) filter ( where ad.status = 'PENDING' ) as pending_application_count
                                                            from bi.p_application_data ad
                                                            where ad.contributor_id = c.contributor_id
                                                            group by ad.contributor_id, ad.project_id) ad on ad.contributor_id = coalesce(cd.contributor_id, rd.contributor_id) and ad.project_id = coalesce(cd.project_id, rd.project_id)
            
                                        where cd.contributor_id = c.contributor_id
                                           or rd.contributor_id = c.contributor_id
                                           or ad.contributor_id = c.contributor_id
                                        group by cd.contributor_id, cd.project_id,
                                                 rd.contributor_id, rd.project_id,
                                                 ad.contributor_id, ad.project_id,
                                                 rd.reward_count,
                                                 rd.total_rewarded_usd_amount,
                                                 ad.application_count,
                                                 ad.pending_application_count) ppd ON true
            
                     LEFT JOIN bi.p_project_global_data p ON p.project_id = ppd.project_id
            
            WHERE c.contributor_login in (:contributorLogins)
            GROUP BY c.contributor_id, tlgm.contact
            """,
            nativeQuery = true)
    List<BOContributorBiReadEntity> findAll(List<String> contributorLogins);

}

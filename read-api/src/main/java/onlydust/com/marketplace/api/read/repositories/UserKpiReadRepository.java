package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.user.UserKpiReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

public interface UserKpiReadRepository extends JpaRepository<UserKpiReadEntity, Long> {

    @Query(nativeQuery = true, value = """
            select au.github_user_id,
                   (select count(distinct pl.project_id)
                    from project_leads pl
                    where pl.user_id = au.user_id
                      and pl.assigned_at >= :startDate and pl.assigned_at <= :endDate) maintainedProjects,
                   (select count(distinct pgr.project_id)
                    from indexer_exp.contributions c
                             join project_github_repos pgr on pgr.github_repo_id = c.repo_id
                    where c.contributor_id = au.github_user_id
                      and (
                            (c.created_at >= :startDate or c.created_at <= :endDate ) or (c.completed_at >= :startDate or c.completed_at <= :endDate)
                          )
                    ) contributedProjects,
                   (select count(distinct r.id)
                    from rewards r
                    where r.recipient_id = au.github_user_id
                      and r.requested_at >= :startDate and r.requested_at <= :endDate) rewards,
                   (select count(distinct gpr.id)
                    from indexer_exp.github_pull_requests gpr
                             join project_github_repos pgr on pgr.github_repo_id = gpr.repo_id
                    where gpr.author_id = au.github_user_id
                      and gpr.status = 'MERGED'
                      and gpr.merged_at >= :startDate and gpr.merged_at <= :endDate) mergedPRs,
                   (select count(distinct gia.issue_id)
                    from indexer_exp.github_issues_assignees gia
                             join indexer_exp.github_issues gi on gi.id = gia.issue_id and gi.status = 'COMPLETED'
                             join project_github_repos pgr on pgr.github_repo_id = gi.repo_id
                    where gia.user_id = au.github_user_id
                      and gi.closed_at >= :startDate and gi.closed_at <= :endDate) resolvedIssues,
                   (select count(a.id)
                    from applications a
                    where a.applicant_id = au.github_user_id
                      and a.received_at >= :startDate and a.received_at <= :endDate) pendingApplications
            from iam.all_users au
            where au.github_user_id = :githubUserId
            """)
    Optional<UserKpiReadEntity> findByGithubUserIdAndDateRange(Long githubUserId, ZonedDateTime startDate, ZonedDateTime endDate);
}

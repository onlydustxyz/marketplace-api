package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ApplicationsToReviewQueryEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface ApplicationsToReviewRepository extends Repository<ApplicationsToReviewQueryEntity, UUID> {

    @Query(value = """
            select applications_grouped_by_project.lead_id                                          as user_id,
                   applications_grouped_by_project.lead_github_login                                as github_login,
                   applications_grouped_by_project.lead_email                                       as email,
                   jsonb_agg(jsonb_build_object('id', applications_grouped_by_project.project_id,
                                                'slug', applications_grouped_by_project.project_slug,
                                                'name', applications_grouped_by_project.project_name,
                                                'issues', applications_grouped_by_project.issues))  as projects
            from (select applications_grouped_by_issue.lead_id,
                         applications_grouped_by_issue.lead_github_login,
                         applications_grouped_by_issue.lead_email,
                         applications_grouped_by_issue.project_id,
                         applications_grouped_by_issue.project_name,
                         applications_grouped_by_issue.project_slug,
                         jsonb_agg(jsonb_build_object('id', applications_grouped_by_issue.issue_id,
                                                      'title', applications_grouped_by_issue.issue_title,
                                                      'repoName', applications_grouped_by_issue.repo_name,
                                                      'applicantCount', applications_grouped_by_issue.application_count)) as issues
                  from (select lead.id           as lead_id,
                               lead.github_login as lead_github_login,
                               lead.email        as lead_email,
                               p.id              as project_id,
                               p.name            as project_name,
                               p.slug            as project_slug,
                               gi.id             as issue_id,
                               gi.title          as issue_title,
                               gi.repo_name      as repo_name,
                               count(a.id)       as application_count
                        from applications a
                                 join indexer_exp.github_issues gi on gi.id = a.issue_id
                                 left join indexer_exp.github_issues_assignees gia on gia.issue_id = gi.id
                                 join projects p on p.id = a.project_id
                                 join project_leads pl on pl.project_id = p.id
                                 join iam.users lead on lead.id = pl.user_id
                        where gia.user_id is null
                        group by lead.id, p.id, gi.id) as applications_grouped_by_issue
                        
                  group by applications_grouped_by_issue.lead_id,
                           applications_grouped_by_issue.lead_github_login,
                           applications_grouped_by_issue.lead_email,
                           applications_grouped_by_issue.project_id,
                           applications_grouped_by_issue.project_name,
                           applications_grouped_by_issue.project_slug) as applications_grouped_by_project
                        
            group by applications_grouped_by_project.lead_id,
                     applications_grouped_by_project.lead_github_login,
                     applications_grouped_by_project.lead_email;
            """, nativeQuery = true)
    List<ApplicationsToReviewQueryEntity> findAllProjectApplicationsToReview();
}

package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.github.ProjectGithubIssueItemReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ProjectGithubIssueItemReadRepository extends Repository<ProjectGithubIssueItemReadEntity, Long> {

    @Query(value = """
            select i.*,
                   p.id project_id,
                   p.name project_name,
                   p.slug project_slug,
                   p.logo_url project_logo_url,
                   assignees.users assignees,
                   applications.applications,
                   labels.strings labels,
                   jsonb_build_object(
                                       'githubUserId', author_account.id,
                                       'login', author_account.login,
                                       'avatarUrl', user_avatar_url(author_account.id, author_account.avatar_url),
                                       'isRegistered', od_author.id is not null
                                    ) author,
                    jsonb_build_object(
                                        'id', gr.id,
                                        'owner', gr.owner_login,
                                        'name', gr.name,
                                        'description', gr.description,
                                        'htmlUrl', gr.html_url
                                      ) repo
            FROM projects p
                    JOIN project_github_repos pgr ON pgr.project_id = p.id
                    JOIN indexer_exp.github_repos gr on gr.id = pgr.github_repo_id
                    JOIN indexer_exp.github_issues i on i.repo_id = pgr.github_repo_id
                    JOIN indexer_exp.github_accounts author_account on i.author_id = author_account.id
                    LEFT JOIN iam.users od_author on od_author.github_user_id = author_account.id
                     LEFT JOIN repo_languages rl ON rl.repo_id = i.repo_id
                     LEFT JOIN (select gia.issue_id,
                                       jsonb_agg(
                                               jsonb_build_object(
                                                       'githubUserId', ga.id,
                                                       'login', ga.login,
                                                       'avatarUrl', user_avatar_url(ga.id, ga.avatar_url)
                                               )
                                       ) users
                                from indexer_exp.github_issues_assignees gia
                                         LEFT JOIN indexer_exp.github_accounts ga on ga.id = gia.user_id
                                group by gia.issue_id) assignees on assignees.issue_id = i.id
                     LEFT JOIN indexer_exp.github_issues_labels gil ON i.id = gil.issue_id
                     LEFT JOIN indexer_exp.github_labels gl on gil.label_id = gl.id
                     LEFT JOIN hackathon_issues h ON h.issue_id = i.id
                     LEFT JOIN (select a.issue_id,
                                       jsonb_agg(
                                               jsonb_build_object(
                                                       'id', a.id,
                                                       'motivations', a.motivations,
                                                       'problemSolvingApproach', a.problem_solving_approach,
                                                       'applicant', jsonb_build_object(
                                                               'githubUserId', ga2.id,
                                                               'login', ga2.login,
                                                               'avatarUrl', user_avatar_url(ga2.id, ga2.avatar_url),
                                                               'isRegistered', u.id is not null
                                                                    )
                                               )
                                       ) applications
                                from applications a
                                         join indexer_exp.github_accounts ga2 on ga2.id = a.applicant_id
                                         left join iam.users u on u.github_user_id = ga2.id
                                where a.project_id = :projectId
                                group by a.issue_id) applications on applications.issue_id = i.id
                     LEFT JOIN (SELECT gil.issue_id, jsonb_agg(jsonb_build_object(
                                   'name', gl.name,
                                   'description', gl.description
                               )) strings
                        FROM indexer_exp.github_issues_labels gil
                                 JOIN indexer_exp.github_labels gl on gil.label_id = gl.id
                        GROUP BY gil.issue_id) labels on labels.issue_id = i.id
            WHERE p.id = :projectId
              AND (coalesce(:statuses) IS NULL OR i.status = ANY (cast(:statuses as indexer_exp.github_issue_status[])))
              AND (:isAssigned IS NULL
                OR :isAssigned = TRUE AND assignees.users IS NOT NULL
                OR :isAssigned = FALSE AND assignees.users IS NULL)
              AND (:isApplied IS NULL
                OR :isApplied = TRUE AND applications.applications IS NOT NULL
                OR :isApplied = FALSE AND applications.applications IS NULL)
              AND (:isGoodFirstIssue IS NULL
                OR :isGoodFirstIssue = TRUE AND cast(labels.strings as text) ILIKE '%good%first%issue%'
                OR :isGoodFirstIssue = FALSE AND cast(labels.strings as text) NOT ILIKE '%good%first%issue%')
              AND (:isIncludedInAnyHackathon IS NULL
                OR :isIncludedInAnyHackathon = TRUE AND h.hackathon_id IS NOT NULL
                OR :isIncludedInAnyHackathon = FALSE AND NOT exists(SELECT 1 FROM hackathon_issues h2 WHERE h2.issue_id = i.id))
              AND (:hackathonId IS NULL OR h.hackathon_id = :hackathonId)
              AND (coalesce(:languageIds) IS NULL OR rl.language_id = ANY (:languageIds))
              AND (coalesce(:search) IS NULL OR i.title ILIKE '%' || :search || '%')
            GROUP BY i.id, assignees.users, applications.applications, p.id, author_account.id, od_author.id, gr.id, labels.strings
            """, nativeQuery = true)
    Page<ProjectGithubIssueItemReadEntity> findIssuesOf(@NonNull UUID projectId,
                                                        String[] statuses,
                                                        Boolean isAssigned,
                                                        Boolean isApplied,
                                                        Boolean isGoodFirstIssue,
                                                        Boolean isIncludedInAnyHackathon,
                                                        UUID hackathonId,
                                                        UUID[] languageIds,
                                                        String search,
                                                        Pageable pageable);
}

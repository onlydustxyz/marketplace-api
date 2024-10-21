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
                   p.id                                             project_id,
                   p.name                                           project_name,
                   p.slug                                           project_slug,
                   p.logo_url                                       project_logo_url,
                   (select jsonb_agg(
                                   jsonb_build_object(
                                           'githubUserId', ga.id,
                                           'login', ga.login,
                                           'avatarUrl', user_avatar_url(ga.id, ga.avatar_url)
                                   )
                           ) users
                    from indexer_exp.github_accounts ga
                    where ga.id = any (ccd.assignee_ids))           assignees,
                   (select jsonb_agg(
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
                    where a.applicant_id = any (ccd.applicant_ids)
                      and a.issue_id = c.issue_id)                  applications,
                   c.github_labels                                  labels,
                   ccd.github_author                                author,
                   c.github_repo                                    repo
            FROM projects p
                     JOIN bi.p_project_global_data pgd ON pgd.project_id = p.id
                     JOIN bi.p_contribution_data c ON c.project_id = p.id AND c.is_issue = 1
                     JOIN bi.p_contribution_contributors_data ccd ON ccd.contribution_uuid = c.contribution_uuid
                     JOIN indexer_exp.github_issues i on i.id = c.issue_id
                     LEFT JOIN hackathons h ON h.github_labels && (select array_agg(l.label) from (select jsonb_array_elements(c.github_labels) ->> 'name' as label) l)
            WHERE p.id = :projectId
              AND (coalesce(:statuses) IS NULL OR c.contribution_status = ANY (cast(:statuses as indexer_exp.contribution_status[])))
              AND (:isAssigned IS NULL
                OR :isAssigned = TRUE AND array_length(ccd.assignee_ids, 1) > 0
                OR :isAssigned = FALSE AND (ccd.assignee_ids IS NULL OR array_length(ccd.assignee_ids, 1) = 0))
              AND (:isApplied IS NULL
                OR :isApplied = TRUE AND array_length(ccd.applicant_ids, 1) > 0
                OR :isApplied = FALSE AND (ccd.applicant_ids IS NULL OR array_length(ccd.applicant_ids, 1) = 0))
              AND (:isAvailable IS NULL
                OR :isAvailable = TRUE AND c.contribution_status = 'IN_PROGRESS' AND (ccd.assignee_ids IS NULL OR array_length(ccd.assignee_ids, 1) = 0)
                OR :isAvailable = FALSE AND (c.contribution_status != 'IN_PROGRESS' OR array_length(ccd.assignee_ids, 1) > 0))
              AND (:isGoodFirstIssue IS NULL
                OR :isGoodFirstIssue = TRUE AND c.is_good_first_issue
                OR :isGoodFirstIssue = FALSE AND NOT c.is_good_first_issue)
              AND (:isIncludedInAnyHackathon IS NULL
                OR :isIncludedInAnyHackathon = TRUE AND h.id IS NOT NULL
                OR :isIncludedInAnyHackathon = FALSE AND NOT exists(SELECT 1 FROM hackathon_issues h2 WHERE h2.issue_id = c.issue_id))
              AND (:hackathonId IS NULL OR h.id = :hackathonId)
              AND (coalesce(:languageIds) IS NULL OR pgd.language_ids && cast(:languageIds as uuid[]))
              AND (coalesce(:search) IS NULL OR c.github_title ILIKE '%' || :search || '%')
            GROUP BY p.id, pgd.project_id, c.contribution_uuid, ccd.contribution_uuid, i.id
            """,
            countQuery = """
                    select count(c.contribution_uuid)
                    FROM projects p
                             JOIN bi.p_project_global_data pgd ON pgd.project_id = p.id
                             JOIN bi.p_contribution_data c ON c.project_id = p.id AND c.is_issue = 1
                             JOIN bi.p_contribution_contributors_data ccd ON ccd.contribution_uuid = c.contribution_uuid
                             LEFT JOIN hackathons h ON h.github_labels && (select array_agg(l.label) from (select jsonb_array_elements(c.github_labels)->>'name' as label) l)
                    WHERE p.id = :projectId
                      AND (coalesce(:statuses) IS NULL OR c.contribution_status = ANY (cast(:statuses as indexer_exp.contribution_status[])))
                      AND (:isAssigned IS NULL
                        OR :isAssigned = TRUE AND array_length(ccd.assignee_ids, 1) > 0
                        OR :isAssigned = FALSE AND (ccd.assignee_ids IS NULL OR array_length(ccd.assignee_ids, 1) = 0))
                      AND (:isApplied IS NULL
                        OR :isApplied = TRUE AND array_length(ccd.applicant_ids, 1) > 0
                        OR :isApplied = FALSE AND (ccd.applicant_ids IS NULL OR array_length(ccd.applicant_ids, 1) = 0))
                      AND (:isAvailable IS NULL
                        OR :isAvailable = TRUE AND c.contribution_status = 'IN_PROGRESS' AND (ccd.assignee_ids IS NULL OR array_length(ccd.assignee_ids, 1) = 0)
                        OR :isAvailable = FALSE AND (c.contribution_status != 'IN_PROGRESS' OR array_length(ccd.assignee_ids, 1) > 0))
                      AND (:isGoodFirstIssue IS NULL
                        OR :isGoodFirstIssue = TRUE AND c.is_good_first_issue
                        OR :isGoodFirstIssue = FALSE AND NOT c.is_good_first_issue)
                      AND (:isIncludedInAnyHackathon IS NULL
                        OR :isIncludedInAnyHackathon = TRUE AND h.id IS NOT NULL
                        OR :isIncludedInAnyHackathon = FALSE AND NOT exists(SELECT 1 FROM hackathon_issues h2 WHERE h2.issue_id = c.issue_id))
                      AND (:hackathonId IS NULL OR h.id = :hackathonId)
                      AND (coalesce(:languageIds) IS NULL OR pgd.language_ids && cast(:languageIds as uuid[]))
                      AND (coalesce(:search) IS NULL OR c.github_title ILIKE '%' || :search || '%')
                    GROUP BY p.id, c.contribution_uuid, ccd.contribution_uuid
                    """, nativeQuery = true)
    Page<ProjectGithubIssueItemReadEntity> findIssuesOf(@NonNull UUID projectId,
                                                        String[] statuses,
                                                        Boolean isAssigned,
                                                        Boolean isApplied,
                                                        Boolean isAvailable,
                                                        Boolean isGoodFirstIssue,
                                                        Boolean isIncludedInAnyHackathon,
                                                        UUID hackathonId,
                                                        UUID[] languageIds,
                                                        String search,
                                                        Pageable pageable);
}

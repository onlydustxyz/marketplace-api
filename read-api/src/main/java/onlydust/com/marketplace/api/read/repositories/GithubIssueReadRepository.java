package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.contract.model.GithubIssueStatus;
import onlydust.com.marketplace.api.read.entities.github.GithubIssueReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface GithubIssueReadRepository extends Repository<GithubIssueReadEntity, Long> {

    @Query("""
            SELECT i
            FROM GithubIssueReadEntity i
            JOIN FETCH i.author
            JOIN FETCH i.repo r
            JOIN FETCH r.owner
            LEFT JOIN FETCH i.applications
            WHERE i.id = :issueId
            """)
    Optional<GithubIssueReadEntity> findById(Long issueId);

    @Query(value = """
            SELECT i.*
            FROM indexer_exp.github_issues i
                     JOIN project_github_repos pgr ON pgr.github_repo_id = i.repo_id
                     LEFT JOIN repo_languages rl ON rl.repo_id = i.repo_id
                     LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = i.id
                     LEFT JOIN indexer_exp.github_issues_labels gil ON i.id = gil.issue_id
                     LEFT JOIN indexer_exp.github_labels gl on gil.label_id = gl.id
                     LEFT JOIN indexer_exp.github_issues_labels gil_hackathon ON i.id = gil_hackathon.issue_id
                     LEFT JOIN indexer_exp.github_labels gl_hackathon ON gil_hackathon.label_id = gl_hackathon.id
                     LEFT JOIN hackathons h ON gl_hackathon.name = ANY (h.github_labels)
                     LEFT JOIN applications a ON a.issue_id = i.id AND a.project_id = pgr.project_id
            WHERE pgr.project_id = :projectId
              AND (coalesce(:statuses) IS NULL OR i.status = ANY (cast(:statuses as indexer_exp.github_issue_status[])))
              AND (:isAssigned IS NULL
                OR :isAssigned = TRUE AND gia.user_id IS NOT NULL
                OR :isAssigned = FALSE AND gia.user_id IS NULL)
              AND (:isApplied IS NULL
                OR :isApplied = TRUE AND a.id IS NOT NULL
                OR :isApplied = FALSE AND a.id IS NULL)
              AND (:isGoodFirstIssue IS NULL
                OR :isGoodFirstIssue = TRUE AND gl.name ILIKE '%good%first%issue%'
                OR :isGoodFirstIssue = FALSE AND gl.name NOT ILIKE '%good%first%issue%')
              AND (:isIncludedInAnyHackathon IS NULL
                OR :isIncludedInAnyHackathon = TRUE AND h.id IS NOT NULL
                OR :isIncludedInAnyHackathon = FALSE AND NOT exists(SELECT 1
                                                                    FROM hackathons h2
                                                                             JOIN indexer_exp.github_labels gl2 ON gl2.name = ANY (h2.github_labels)
                                                                             JOIN indexer_exp.github_issues_labels gil2 ON gil2.label_id = gl2.id
                                                                    WHERE gil2.issue_id = i.id))
              AND (:hackathonId IS NULL OR h.id = :hackathonId)
              AND (coalesce(:languageIds) IS NULL OR rl.language_id = ANY (:languageIds))
              AND (coalesce(:search) IS NULL OR i.title ILIKE '%' || :search || '%')
            GROUP BY i.id
            """, nativeQuery = true)
    Page<GithubIssueReadEntity> findIssuesOf(UUID projectId,
                                             String[] statuses,
                                             Boolean isAssigned,
                                             Boolean isApplied,
                                             Boolean isGoodFirstIssue,
                                             Boolean isIncludedInAnyHackathon,
                                             UUID hackathonId,
                                             UUID[] languageIds,
                                             String search,
                                             Pageable pageable);

    @Query("""
            SELECT i
            FROM GithubIssueReadEntity i
            JOIN FETCH i.repo r
            JOIN FETCH i.author
            JOIN r.projects p
            WHERE p.id = :projectId AND
            (coalesce(:status, null) IS NULL OR i.status = :status) AND
            (:isAssigned IS NULL OR (:isAssigned = TRUE AND size(i.assignees) > 0) OR (:isAssigned = FALSE AND size(i.assignees) = 0)) AND
            (:isApplied IS NULL OR (:isApplied = TRUE AND exists(from ApplicationReadEntity a where a.issueId = i.id and a.projectId = p.id)) OR (:isApplied = FALSE AND not exists(from ApplicationReadEntity a where a.issueId = i.id and a.projectId = p.id)))
            """)
    Page<GithubIssueReadEntity> findAllOf(UUID projectId,
                                          GithubIssueStatus status,
                                          Boolean isAssigned,
                                          Boolean isApplied,
                                          Pageable pageable);
}

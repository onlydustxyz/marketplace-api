package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonProjectIssuesReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface HackathonProjectIssuesReadRepository extends Repository<HackathonProjectIssuesReadEntity, UUID> {

    @Query(value = """
            SELECT p.id,
                   p.name,
                   p.logo_url,
                   p.slug,
                   count(distinct i.id) as issue_count
            FROM indexer_exp.github_issues i
                     JOIN project_github_repos pgr ON pgr.github_repo_id = i.repo_id
                     JOIN projects p ON p.id = pgr.project_id
                     JOIN hackathon_issues h ON h.issue_id = i.id
                     JOIN indexer_exp.github_accounts author_account on i.author_id = author_account.id
                     LEFT JOIN repo_languages rl ON rl.repo_id = i.repo_id
                     LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = i.id
                     LEFT JOIN indexer_exp.github_issues_labels gil ON i.id = gil.issue_id
                     LEFT JOIN indexer_exp.github_labels gl ON gil.label_id = gl.id
                     LEFT JOIN applications a ON a.issue_id = i.id AND a.project_id = pgr.project_id
            WHERE h.hackathon_id = :hackathonId
              AND (coalesce(:statuses) IS NULL OR i.status = ANY (cast(:statuses as indexer_exp.github_issue_status[])))
              AND (:isAssigned IS NULL
                OR :isAssigned = TRUE AND gia.user_id IS NOT NULL
                OR :isAssigned = FALSE AND gia.user_id IS NULL)
              AND (:isApplied IS NULL
                OR :isApplied = TRUE AND a.id IS NOT NULL
                OR :isApplied = FALSE AND a.id IS NULL)
              AND (:isAvailable IS NULL
                OR :isAvailable = TRUE AND i.status = 'OPEN' AND gia.user_id IS NULL
                OR :isAvailable = FALSE AND (i.status != 'OPEN' OR gia.user_id IS NOT NULL))
              AND (:isGoodFirstIssue IS NULL
                OR :isGoodFirstIssue = TRUE AND gl.name ILIKE '%good%first%issue%'
                OR :isGoodFirstIssue = FALSE AND gl.name NOT ILIKE '%good%first%issue%')
              AND (coalesce(:languageIds) IS NULL OR rl.language_id = ANY (:languageIds))
              AND (coalesce(:search) IS NULL OR i.title ILIKE '%' || :search || '%')
            GROUP BY p.id
            """, nativeQuery = true)
    List<HackathonProjectIssuesReadEntity> findAll(@NonNull UUID hackathonId,
                                                   String[] statuses,
                                                   Boolean isAssigned,
                                                   Boolean isApplied,
                                                   Boolean isAvailable,
                                                   Boolean isGoodFirstIssue,
                                                   UUID[] languageIds,
                                                   String search);
}

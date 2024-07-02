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

    @Query("""
            SELECT i
            FROM GithubIssueReadEntity i
            JOIN i.goodFirstIssueOf p
            JOIN FETCH i.author
            JOIN FETCH i.repo
            WHERE p.projectId = :projectId
            """)
    Page<GithubIssueReadEntity> findGoodFirstIssuesOf(UUID projectId, Pageable pageable);

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

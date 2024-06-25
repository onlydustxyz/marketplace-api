package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.github.GithubIssueReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface GithubIssueReadRepository extends Repository<GithubIssueReadEntity, Long> {

    Optional<GithubIssueReadEntity> findById(Long issueId);

    @Query("""
            SELECT i
            FROM GithubIssueReadEntity i
            JOIN FETCH i.goodFirstIssueOf p
            JOIN FETCH i.author
            JOIN FETCH i.repo
            WHERE p.id = :projectId
            """)
    Page<GithubIssueReadEntity> findGoodFirstIssuesOf(UUID projectId, Pageable pageable);

    @Query("""
            SELECT i
            FROM GithubIssueReadEntity i
            JOIN i.repo.projects p
            WHERE p.id = :projectId AND
            (:isAssigned IS NULL OR (:isAssigned = TRUE AND size(i.assignees) > 0) OR (:isAssigned = FALSE AND size(i.assignees) = 0)) AND
            (:isApplied IS NULL OR (:isApplied = TRUE AND size(i.applications) > 0) OR (:isApplied = FALSE AND size(i.applications) = 0))
            """)
    Page<GithubIssueReadEntity> findAllOf(UUID projectId,
                                          Boolean isAssigned,
                                          Boolean isApplied,
                                          Pageable pageable);
}

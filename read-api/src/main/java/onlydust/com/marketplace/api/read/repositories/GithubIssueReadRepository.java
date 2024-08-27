package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.github.GithubIssueReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

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
}

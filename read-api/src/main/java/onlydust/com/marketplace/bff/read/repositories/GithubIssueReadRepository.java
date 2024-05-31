package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.github.GithubIssueReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface GithubIssueReadRepository extends Repository<GithubIssueReadEntity, UUID> {
    @Query("""
            SELECT i
            FROM GithubIssueReadEntity i
            JOIN i.goodFirstIssueOf p
            WHERE p.id = :id
            """)
    Page<GithubIssueReadEntity> findGoodFirstIssuesOf(UUID id, Pageable pageable);
}

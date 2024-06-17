package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubIssueViewEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface GithubIssueViewRepository extends Repository<GithubIssueViewEntity, Long> {
    Optional<GithubIssueViewEntity> findById(Long id);
}

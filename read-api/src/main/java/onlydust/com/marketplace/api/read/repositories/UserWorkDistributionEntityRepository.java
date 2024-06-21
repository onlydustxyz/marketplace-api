package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.user.UserWorkDistributionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface UserWorkDistributionEntityRepository extends Repository<UserWorkDistributionEntity, String> {
    @Query(value = """
            SELECT stats.contributor_id,
                   stats.code_review_count,
                   stats.issue_count,
                   stats.pull_request_count
            FROM contributions_stats_per_ecosystem_per_user stats
            WHERE stats.contributor_id = :githubUserId
              AND stats.ecosystem_id = :ecosystemId
            """, nativeQuery = true)
    Optional<UserWorkDistributionEntity> findByContributorIdAndEcosystem(Long githubUserId, UUID ecosystemId);

    @Query(value = """
            SELECT stats.contributor_id,
                   stats.code_review_count,
                   stats.issue_count,
                   stats.pull_request_count
            FROM contributions_stats_per_user stats
            WHERE stats.contributor_id = :githubUserId
            """, nativeQuery = true)
    Optional<UserWorkDistributionEntity> findByContributorId(Long githubUserId);
}

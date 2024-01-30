package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.LedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LedgerRepository extends JpaRepository<LedgerEntity, UUID> {
    Optional<LedgerEntity> findBySponsorSponsorIdAndCurrencyId(UUID sponsorId, UUID currency);

    Optional<LedgerEntity> findByContributorGithubUserIdAndCurrencyId(Long githubUserId, UUID currency);

    Optional<LedgerEntity> findByProjectProjectIdAndCurrencyId(UUID projectId, UUID currency);
}

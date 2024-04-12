package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SponsorAccountRepository extends JpaRepository<SponsorAccountEntity, UUID> {
    List<SponsorAccountEntity> findAllBySponsorId(UUID sponsorId);

    Optional<SponsorAccountEntity> findBySponsorIdAndCurrencyId(UUID sponsorId, UUID currencyId);
}

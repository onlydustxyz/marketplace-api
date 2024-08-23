package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface SponsorReadRepository extends Repository<SponsorReadEntity, UUID> {
    Optional<SponsorReadEntity> findById(UUID sponsorId);
}

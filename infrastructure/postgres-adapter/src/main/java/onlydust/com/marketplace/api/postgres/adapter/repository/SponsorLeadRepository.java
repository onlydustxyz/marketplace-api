package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorLeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SponsorLeadRepository extends JpaRepository<SponsorLeadEntity, SponsorLeadEntity.PrimaryKey> {

    Optional<SponsorLeadEntity> findByUserId(UUID userId);
}

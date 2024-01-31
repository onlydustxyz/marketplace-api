package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EcosystemRepository extends JpaRepository<EcosystemEntity, UUID> {
}

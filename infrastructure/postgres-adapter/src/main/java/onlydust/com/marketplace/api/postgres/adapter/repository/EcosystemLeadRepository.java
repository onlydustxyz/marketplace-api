package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemLeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EcosystemLeadRepository extends JpaRepository<EcosystemLeadEntity, EcosystemLeadEntity.PrimaryKey> {
    List<EcosystemLeadEntity> findByUserId(UUID userId);
}

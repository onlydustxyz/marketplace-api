package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.ecosystem.EcosystemReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface EcosystemReadRepository extends Repository<EcosystemReadEntity, UUID> {
    Page<EcosystemReadEntity> findAll(Pageable pageable);
}

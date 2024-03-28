package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SponsorRepository extends JpaRepository<SponsorEntity, UUID> {
    Page<SponsorEntity> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}

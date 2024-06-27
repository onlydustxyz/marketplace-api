package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.BannerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BannerRepository extends JpaRepository<BannerEntity, UUID> {
}

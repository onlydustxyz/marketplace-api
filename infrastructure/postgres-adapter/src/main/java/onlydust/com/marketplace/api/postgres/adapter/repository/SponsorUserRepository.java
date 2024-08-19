package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SponsorUserRepository extends JpaRepository<SponsorUserEntity, SponsorUserEntity.PrimaryKey> {
}

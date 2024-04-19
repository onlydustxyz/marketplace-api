package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.HackathonRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRegistrationRepository extends JpaRepository<HackathonRegistrationEntity, HackathonRegistrationEntity.PrimaryKey> {
}

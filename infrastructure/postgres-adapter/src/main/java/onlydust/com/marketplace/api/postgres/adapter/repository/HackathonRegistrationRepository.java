package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.HackathonRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HackathonRegistrationRepository extends JpaRepository<HackathonRegistrationEntity, HackathonRegistrationEntity.PrimaryKey> {
    boolean existsByHackathonId(UUID hackathonId);
}

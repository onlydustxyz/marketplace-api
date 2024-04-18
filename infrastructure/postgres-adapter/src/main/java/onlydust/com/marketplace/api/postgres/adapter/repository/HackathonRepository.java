package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.HackathonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HackathonRepository extends JpaRepository<HackathonEntity, UUID> {
}

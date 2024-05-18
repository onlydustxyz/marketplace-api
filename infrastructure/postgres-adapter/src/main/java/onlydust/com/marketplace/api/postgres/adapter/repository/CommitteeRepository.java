package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommitteeRepository extends JpaRepository<CommitteeEntity, UUID> {
}

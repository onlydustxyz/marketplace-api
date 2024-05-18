package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommitteeApplicationRepository extends JpaRepository<CommitteeApplicationEntity, CommitteeApplicationEntity.PrimaryKey> {

    Optional<CommitteeApplicationEntity> findByCommitteeIdAndProjectIdAndUserId(UUID committeeId, UUID projectId, UUID userId);
}

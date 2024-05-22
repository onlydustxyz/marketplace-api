package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeProjectAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommitteeApplicationRepository extends JpaRepository<CommitteeProjectAnswerEntity, CommitteeProjectAnswerEntity.PrimaryKey> {

    boolean existsByCommitteeIdAndProjectId(UUID committeeId, UUID projectId);
}

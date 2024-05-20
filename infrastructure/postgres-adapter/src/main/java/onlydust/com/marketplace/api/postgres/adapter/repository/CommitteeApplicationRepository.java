package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeProjectAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommitteeApplicationRepository extends JpaRepository<CommitteeProjectAnswerEntity, CommitteeProjectAnswerEntity.PrimaryKey> {

    List<CommitteeProjectAnswerEntity> findByCommitteeIdAndProjectId(UUID committeeId, UUID projectId);
}

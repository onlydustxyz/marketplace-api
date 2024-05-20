package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeProjectAnswerViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommitteeProjectAnswerViewRepository extends JpaRepository<CommitteeProjectAnswerViewEntity, CommitteeProjectAnswerViewEntity.PrimaryKey> {

    List<CommitteeProjectAnswerViewEntity> findByCommitteeIdAndAndProjectId(UUID committeeId, UUID projectId);
}

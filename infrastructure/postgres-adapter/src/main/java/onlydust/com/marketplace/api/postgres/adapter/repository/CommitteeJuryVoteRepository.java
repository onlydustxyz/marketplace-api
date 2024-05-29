package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeJuryVoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommitteeJuryVoteRepository extends JpaRepository<CommitteeJuryVoteEntity, CommitteeJuryVoteEntity.PrimaryKey> {
    List<CommitteeJuryVoteEntity> findAllByCommitteeIdAndProjectId(UUID committeeId, UUID projectId);

    List<CommitteeJuryVoteEntity> findAllByCommitteeId(UUID value);
}

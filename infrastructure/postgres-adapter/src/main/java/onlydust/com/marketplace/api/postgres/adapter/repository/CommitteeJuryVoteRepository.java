package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeJuryVoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitteeJuryVoteRepository extends JpaRepository<CommitteeJuryVoteEntity, CommitteeJuryVoteEntity.PrimaryKey> {
}

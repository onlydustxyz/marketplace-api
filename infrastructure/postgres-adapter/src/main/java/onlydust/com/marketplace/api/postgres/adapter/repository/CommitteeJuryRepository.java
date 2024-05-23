package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeJuryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommitteeJuryRepository extends JpaRepository<CommitteeJuryEntity, CommitteeJuryEntity.PrimaryKey> {
    void deleteAllByCommitteeId(UUID committeeId);
}

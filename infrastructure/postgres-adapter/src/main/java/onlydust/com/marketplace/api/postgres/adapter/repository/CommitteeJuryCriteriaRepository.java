package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeJuryCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommitteeJuryCriteriaRepository extends JpaRepository<CommitteeJuryCriteriaEntity, UUID> {
    void deleteAllByCommitteeId(UUID committeeId);
}

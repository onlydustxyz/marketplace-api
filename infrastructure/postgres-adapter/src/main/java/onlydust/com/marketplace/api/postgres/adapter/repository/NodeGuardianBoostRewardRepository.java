package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.NodeGuardianBoostRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeGuardianBoostRewardRepository extends JpaRepository<NodeGuardianBoostRewardEntity, NodeGuardianBoostRewardEntity.PrimaryKey> {
}

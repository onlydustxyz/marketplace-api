package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardUsdEquivalentDataViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RewardUsdEquivalentDataRepository extends JpaRepository<RewardUsdEquivalentDataViewEntity, UUID> {
}

package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RewardViewRepository extends JpaRepository<RewardViewEntity, UUID> {
    List<RewardViewEntity> findByBillingProfileIdAndStatusStatus(UUID billingProfileId, RewardStatus.Input status);
}

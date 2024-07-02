package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.reward.RewardReadEntity;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RewardReadRepository extends JpaRepository<RewardReadEntity, UUID> {
    boolean existsByRecipientIdAndStatus_Status(Long githubUserId, RewardStatus.Input status);
}

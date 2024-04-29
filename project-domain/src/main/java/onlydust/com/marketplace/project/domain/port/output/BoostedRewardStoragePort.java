package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.view.ShortProjectRewardView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoostedRewardStoragePort {

    void markRewardsAsBoosted(List<UUID> rewardsBoosted, Long recipientId);

    void updateBoostedRewardsWithBoostRewardId(List<UUID> rewardsBoosted, Long recipientId, UUID rewardId);

    Optional<Integer> getBoostedRewardsCountByRecipientId(Long recipientId);

    List<ShortProjectRewardView> getRewardsToBoostFromEcosystemNotLinkedToProject(UUID ecosystemId, UUID projectId);
}

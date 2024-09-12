package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.project.domain.view.ShortProjectRewardView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoostedRewardStoragePort {

    void markRewardsAsBoosted(List<RewardId> rewardsBoosted, Long recipientId);

    void updateBoostedRewardsWithBoostRewardId(List<RewardId> rewardsBoosted, Long recipientId, RewardId rewardId);

    Optional<Integer> getBoostedRewardsCountByRecipientId(Long recipientId);

    List<ShortProjectRewardView> getRewardsToBoostFromEcosystemNotLinkedToProject(UUID ecosystemId, ProjectId projectId);
}

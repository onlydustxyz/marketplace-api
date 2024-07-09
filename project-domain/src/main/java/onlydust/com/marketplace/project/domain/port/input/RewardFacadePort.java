package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;

import java.util.UUID;

public interface RewardFacadePort {
    UUID createReward(UUID projectLeadId, RequestRewardCommand requestRewardCommand);

    void cancelReward(UUID projectLeadId, UUID projectId, UUID rewardId);
}

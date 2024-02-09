package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.model.Reward;

import java.util.Optional;
import java.util.UUID;

public interface RewardFacadePort {

    UUID createReward(UUID projectLeadId,
                      RequestRewardCommand requestRewardCommand);

    void cancelReward(UUID projectLeadId, UUID projectId, UUID rewardId);

    void markInvoiceAsReceived(Long recipientId);

    Optional<Reward> getReward(UUID rewardId);
}

package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.OldPayRewardRequestCommand;
import onlydust.com.marketplace.project.domain.model.OldRequestRewardCommand;
import onlydust.com.marketplace.project.domain.model.Reward;

import java.util.Optional;
import java.util.UUID;

public interface RewardFacadePort {

    UUID createReward(UUID projectLeadId,
                      OldRequestRewardCommand oldRequestRewardCommand);

    void cancelReward(UUID projectLeadId, UUID projectId, UUID rewardId);

    void markInvoiceAsReceived(Long recipientId);

    Optional<Reward> getReward(UUID rewardId);

    void oldPayReward(OldPayRewardRequestCommand oldPayRewardRequestCommand);
}

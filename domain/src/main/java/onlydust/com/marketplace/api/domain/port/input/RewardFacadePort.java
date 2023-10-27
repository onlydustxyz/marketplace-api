package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;

import java.util.UUID;

public interface RewardFacadePort<Authentication> {

    UUID requestPayment(Authentication authentication, UUID projectLeadId,
                        RequestRewardCommand requestRewardCommand);

    void cancelPayment(Authentication authentication, UUID projectLeadId, UUID projectId, UUID rewardId);
}

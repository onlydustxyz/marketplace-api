package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;

import java.util.UUID;

public interface RewardStoragePort<Authentication> {

    UUID requestPayment(Authentication authentication,
                        RequestRewardCommand requestRewardCommand);

    void cancelPayment(Authentication authentication, UUID rewardId);
}

package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;

public interface RewardStoragePort<Authentication> {

    void requestPayment(Authentication authentication,
                        RequestRewardCommand requestRewardCommand);
}

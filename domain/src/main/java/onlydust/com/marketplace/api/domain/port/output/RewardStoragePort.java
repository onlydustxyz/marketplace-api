package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;

public interface RewardStoragePort<JwtPayload> {

    void requestPayment(JwtPayload jwtPayload,
                        RequestRewardCommand requestRewardCommand);
}

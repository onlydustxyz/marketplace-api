package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;

import java.util.UUID;

public interface RewardFacadePort<JwtPayload> {

    void requestPayment(JwtPayload jwtPayload, UUID projectLeadId,
                        RequestRewardCommand requestRewardCommand);
}

package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.api.domain.port.output.RewardStoragePort;

import java.util.UUID;

@AllArgsConstructor
public class RewardService<JwtPayload> implements RewardFacadePort<JwtPayload> {

    private final RewardStoragePort<JwtPayload> rewardStoragePort;

    @Override
    public void requestPayment(JwtPayload jwtPayload, UUID projectLeadId, RequestRewardCommand requestRewardCommand) {

    }
}

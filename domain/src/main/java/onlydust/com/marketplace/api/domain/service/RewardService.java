package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.api.domain.port.output.RewardStoragePort;

import java.util.UUID;

@AllArgsConstructor
public class RewardService<Authentication> implements RewardFacadePort<Authentication> {

    private final RewardStoragePort<Authentication> rewardStoragePort;
    private final PermissionService permissionService;

    @Override
    public void requestPayment(Authentication authentication, UUID projectLeadId,
                               RequestRewardCommand requestRewardCommand) {
        if (permissionService.isUserProjectLead(requestRewardCommand.getProjectId(), projectLeadId)) {
            rewardStoragePort.requestPayment(authentication, requestRewardCommand);
        } else {
            throw OnlyDustException.forbidden("User must be project lead to request a reward");
        }

    }
}

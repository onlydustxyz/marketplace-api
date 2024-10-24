package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;

import java.util.List;

public interface RewardFacadePort {
    RewardId createReward(UserId projectLeadId, RequestRewardCommand requestRewardCommand);

    void cancelReward(UserId projectLeadId, ProjectId projectId, RewardId rewardId);

    void createRewards(UserId projectLeadId, List<RequestRewardCommand> rewardRequestCommands);
}

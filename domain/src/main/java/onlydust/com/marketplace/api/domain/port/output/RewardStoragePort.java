package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.model.Reward;

import java.util.List;

public interface RewardStoragePort {
    void createReward(Reward reward);

    List<Project> listProjectsByRecipient(Long githubUserId);
}

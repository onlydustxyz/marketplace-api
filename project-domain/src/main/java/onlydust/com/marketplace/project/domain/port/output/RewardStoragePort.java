package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Reward;

import java.util.List;
import java.util.Optional;

public interface RewardStoragePort {
    void save(Reward reward);

    void delete(RewardId rewardId);

    Optional<Reward> get(RewardId rewardId);

    List<Project> listProjectsByRecipient(Long githubUserId);
}

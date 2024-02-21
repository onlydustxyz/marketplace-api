package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Reward;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RewardStoragePort {
    void save(Reward reward);

    void delete(UUID rewardId);

    Optional<Reward> get(UUID rewardId);

    List<Project> listProjectsByRecipient(Long githubUserId);
}

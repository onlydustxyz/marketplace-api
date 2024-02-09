package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.model.Reward;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RewardStoragePort {
    void save(Reward reward);

    void delete(UUID rewardId);

    Optional<Reward> get(UUID rewardId);

    List<Project> listProjectsByRecipient(Long githubUserId);
}

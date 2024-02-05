package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.model.Reward;
import onlydust.com.marketplace.api.domain.port.output.RewardStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresRewardV2Adapter implements RewardStoragePort {

    private final RewardRepository rewardRepository;


    @Override
    public void save(Reward reward) {
        rewardRepository.save(RewardEntity.of(reward));
    }

    @Override
    public void delete(UUID rewardId) {
        rewardRepository.deleteById(rewardId);
    }

    @Override
    public Optional<Reward> get(UUID rewardId) {
        return rewardRepository.findById(rewardId).map(RewardEntity::toReward);
    }

    @Override
    public List<Project> listProjectsByRecipient(Long githubUserId) {
        throw OnlyDustException.internalServerError("Not yet implemented for v2");
    }
}

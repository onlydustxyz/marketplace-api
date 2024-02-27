package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.output.RewardStoragePort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresRewardV2Adapter implements RewardStoragePort {

    private final RewardRepository rewardRepository;
    private final CurrencyStorage currencyStorage;


    @Override
    public void save(Reward reward) {
        // TODO remove
        final var currency = currencyStorage.findByCode(Currency.Code.of(reward.currency().name().toUpperCase()))
                .orElseThrow(() -> OnlyDustException.internalServerError("Currency %s not found".formatted(reward.currency().name())));
        rewardRepository.save(RewardEntity.of(reward, currency));
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

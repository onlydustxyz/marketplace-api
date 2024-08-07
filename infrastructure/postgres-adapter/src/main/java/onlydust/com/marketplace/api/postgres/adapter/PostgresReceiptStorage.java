package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Receipt;
import onlydust.com.marketplace.accounting.domain.port.out.ReceiptStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ReceiptEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import org.springframework.transaction.annotation.Transactional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresReceiptStorage implements ReceiptStoragePort {
    private final RewardRepository rewardRepository;

    @Override
    @Transactional
    public void save(Receipt receipt) {
        final var reward = rewardRepository.findById(receipt.rewardId().value())
                .orElseThrow(() -> notFound("Reward %s not found".formatted(receipt.rewardId())));
        reward.receipts().add(ReceiptEntity.of(receipt));
        rewardRepository.saveAndFlush(reward);
    }
}

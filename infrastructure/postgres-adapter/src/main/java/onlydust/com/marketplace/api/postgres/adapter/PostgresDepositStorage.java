package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.port.out.DepositStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.DepositEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.DepositRepository;
import onlydust.com.marketplace.kernel.model.SponsorId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresDepositStorage implements DepositStoragePort {
    private final DepositRepository depositRepository;

    @Override
    @Transactional
    public void save(Deposit deposit) {
        depositRepository.save(DepositEntity.of(deposit));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SponsorId> findDepositSponsor(Deposit.Id depositId) {
        return depositRepository.findById(depositId.value()).map(DepositEntity::sponsorId).map(SponsorId::of);
    }

    @Override
    @Transactional
    public void saveStatusAndBillingInformation(Deposit.Id depositId, Deposit.Status status, Deposit.BillingInformation billingInformation) {
        final var deposit = depositRepository.findById(depositId.value())
                .orElseThrow(() -> notFound("Deposit %s not found".formatted(depositId)));
        deposit.status(status);
        deposit.billingInformation(billingInformation);
    }
}

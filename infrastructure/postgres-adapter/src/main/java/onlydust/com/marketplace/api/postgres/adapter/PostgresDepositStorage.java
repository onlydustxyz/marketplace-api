package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.port.out.DepositStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.DepositEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.DepositRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
public class PostgresDepositStorage implements DepositStoragePort {
    private final DepositRepository depositRepository;

    @Override
    @Transactional
    public void save(Deposit deposit) {
        depositRepository.save(DepositEntity.of(deposit));
    }

    @Override
    public Optional<Deposit> find(Deposit.Id depositId) {
        return depositRepository.findById(depositId.value()).map(DepositEntity::toDomain);
    }

    @Override
    public Optional<Deposit> findByTransactionReference(@NonNull String transactionReference) {
        return depositRepository.findByTransactionReference(transactionReference).map(DepositEntity::toDomain);
    }
}

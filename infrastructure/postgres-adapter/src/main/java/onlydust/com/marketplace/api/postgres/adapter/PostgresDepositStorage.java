package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.port.out.DepositStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.DepositEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.DepositRepository;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class PostgresDepositStorage implements DepositStoragePort {
    private final DepositRepository depositRepository;

    @Override
    @Transactional
    public void save(Deposit deposit) {
        depositRepository.save(DepositEntity.of(deposit));
    }
}

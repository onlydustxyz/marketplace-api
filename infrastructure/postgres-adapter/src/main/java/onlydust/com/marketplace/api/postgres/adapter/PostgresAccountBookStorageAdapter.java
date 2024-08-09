package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookTransactionProjection;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookTransactionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookTransactionRepository;

@AllArgsConstructor
public class PostgresAccountBookStorageAdapter implements AccountBookStorage {
    private final @NonNull AccountBookTransactionRepository accountBookTransactionRepository;

    @Override
    public void save(AccountBookTransactionProjection projection) {
        accountBookTransactionRepository.save(AccountBookTransactionEntity.fromDomain(projection));
    }
}

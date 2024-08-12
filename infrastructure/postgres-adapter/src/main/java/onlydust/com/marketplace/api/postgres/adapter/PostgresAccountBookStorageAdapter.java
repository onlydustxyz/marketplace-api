package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookTransactionProjection;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookTransactionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookTransactionRepository;

import java.util.Optional;

@AllArgsConstructor
public class PostgresAccountBookStorageAdapter implements AccountBookStorage {
    private final @NonNull AccountBookRepository accountBookRepository;
    private final @NonNull AccountBookTransactionRepository accountBookTransactionRepository;

    @Override
    public void save(AccountBookTransactionProjection projection) {
        accountBookTransactionRepository.save(AccountBookTransactionEntity.fromDomain(projection));
    }

    @Override
    public Optional<AccountBookAggregate> get(Currency.Id currencyId) {
        return accountBookRepository.findByCurrencyId(currencyId.value())
                .map(AccountBookEntity::toDomain);
    }
}

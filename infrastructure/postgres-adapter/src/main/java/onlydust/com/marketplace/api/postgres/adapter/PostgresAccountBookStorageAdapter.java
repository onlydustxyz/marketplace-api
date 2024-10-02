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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
public class PostgresAccountBookStorageAdapter implements AccountBookStorage {
    private final @NonNull AccountBookRepository accountBookRepository;
    private final @NonNull AccountBookTransactionRepository accountBookTransactionRepository;

    @Override
    @Transactional
    public void save(AccountBookTransactionProjection projection) {
        if (projection.depositStatus() != null) {
            accountBookTransactionRepository.save(AccountBookTransactionEntity.fromDomain(projection));
            return;
        }
        
        accountBookTransactionRepository.findByTimestampAndTypeAndCurrencyIdAndSponsorIdAndProgramIdAndProjectIdAndRewardIdAndPaymentId(
                projection.timestamp(),
                projection.type(),
                projection.currencyId().value(),
                projection.sponsorId().value(),
                projection.programId() == null ? null : projection.programId().value(),
                projection.projectId() == null ? null : projection.projectId().value(),
                projection.rewardId() == null ? null : projection.rewardId().value(),
                projection.paymentId() == null ? null : projection.paymentId().value()
        ).ifPresentOrElse(
                entity -> entity.amount(entity.amount().add(projection.amount().getValue())),
                () -> accountBookTransactionRepository.save(AccountBookTransactionEntity.fromDomain(projection))
        );
    }

    @Override
    public Optional<AccountBookAggregate> get(Currency.Id currencyId) {
        return accountBookRepository.findByCurrencyId(currencyId.value())
                .map(AccountBookEntity::toDomain);
    }
}

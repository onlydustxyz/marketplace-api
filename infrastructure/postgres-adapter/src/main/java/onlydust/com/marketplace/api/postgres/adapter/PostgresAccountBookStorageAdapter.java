package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountingTransactionProjection;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AllTransactionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.AllTransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
public class PostgresAccountBookStorageAdapter implements AccountBookStorage {
    private final @NonNull AccountBookRepository accountBookRepository;
    private final @NonNull AllTransactionRepository allTransactionRepository;

    @Override
    @Transactional
    public void save(AccountingTransactionProjection projection) {
        if (projection.depositStatus() != null) {
            allTransactionRepository.save(AllTransactionEntity.fromDomain(projection));
            return;
        }

        allTransactionRepository.findByTimestampAndTypeAndCurrencyIdAndSponsorIdAndProgramIdAndProjectIdAndRewardIdAndPaymentId(
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
                () -> allTransactionRepository.save(AllTransactionEntity.fromDomain(projection))
        );
    }

    @Override
    public Optional<AccountBookAggregate> get(Currency.Id currencyId) {
        return accountBookRepository.findByCurrencyId(currencyId.value())
                .map(AccountBookEntity::toDomain);
    }
}

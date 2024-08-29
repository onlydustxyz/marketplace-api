package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AccountBookTransactionRepository extends JpaRepository<AccountBookTransactionEntity, Long> {
    Optional<AccountBookTransactionEntity> findByTimestampAndTypeAndCurrencyIdAndSponsorIdAndProgramIdAndProjectIdAndRewardIdAndPaymentId(
            ZonedDateTime timestamp,
            AccountBook.Transaction.Type type,
            UUID currencyId,
            UUID sponsorId,
            UUID programId,
            UUID projectId,
            UUID rewardId,
            UUID paymentId
    );
}

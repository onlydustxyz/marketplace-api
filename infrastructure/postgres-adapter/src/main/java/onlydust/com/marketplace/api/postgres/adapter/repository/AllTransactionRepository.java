package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountingTransactionProjection;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AllTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AllTransactionRepository extends JpaRepository<AllTransactionEntity, Long> {
    Optional<AllTransactionEntity> findByTimestampAndTypeAndCurrencyIdAndSponsorIdAndProgramIdAndProjectIdAndRewardIdAndPaymentId(
            ZonedDateTime timestamp,
            AccountingTransactionProjection.Type type,
            UUID currencyId,
            UUID sponsorId,
            UUID programId,
            UUID projectId,
            UUID rewardId,
            UUID paymentId
    );
}

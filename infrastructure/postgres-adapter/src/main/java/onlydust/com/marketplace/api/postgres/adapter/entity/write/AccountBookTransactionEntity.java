package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookTransactionProjection;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_book_transactions", schema = "accounting")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Accessors(fluent = true)
public class AccountBookTransactionEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long index;

    @NonNull
    ZonedDateTime timestamp;

    @NonNull
    UUID accountBookId;

    UUID sponsorAccountId;

    UUID projectId;

    UUID rewardId;

    UUID paymentId;

    @NonNull
    BigDecimal amount;

    public static AccountBookTransactionEntity fromDomain(AccountBookTransactionProjection projection) {
        return new AccountBookTransactionEntity(
                null,
                projection.timestamp(),
                projection.accountBookId().value(),
                projection.sponsorAccountId() == null ? null : projection.sponsorAccountId().value(),
                projection.projectId() == null ? null : projection.projectId().value(),
                projection.rewardId() == null ? null : projection.rewardId().value(),
                projection.paymentId() == null ? null : projection.paymentId().value(),
                projection.amount().getValue()
        );
    }
}

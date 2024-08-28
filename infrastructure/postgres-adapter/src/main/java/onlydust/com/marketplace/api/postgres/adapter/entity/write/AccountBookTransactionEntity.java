package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookTransactionProjection;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_book_transactions", schema = "accounting")
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Setter
@Accessors(fluent = true)
public class AccountBookTransactionEntity {
    @Id
    UUID id;

    @NonNull
    ZonedDateTime timestamp;

    @NonNull
    UUID currencyId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "transaction_type")
    @NonNull
    AccountBook.Transaction.Type type;

    UUID sponsorId;

    UUID programId;

    UUID projectId;

    UUID rewardId;

    UUID paymentId;

    @NonNull
    BigDecimal amount;

    public static AccountBookTransactionEntity fromDomain(AccountBookTransactionProjection projection) {
        return new AccountBookTransactionEntity(
                UUID.randomUUID(),
                projection.timestamp(),
                projection.currencyId().value(),
                projection.type(),
                projection.sponsorId().value(),
                projection.programId() == null ? null : projection.programId().value(),
                projection.projectId() == null ? null : projection.projectId().value(),
                projection.rewardId() == null ? null : projection.rewardId().value(),
                projection.paymentId() == null ? null : projection.paymentId().value(),
                projection.amount().getValue()
        );
    }
}

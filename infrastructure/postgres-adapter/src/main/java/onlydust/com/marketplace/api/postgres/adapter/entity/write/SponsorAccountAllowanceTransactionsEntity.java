package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Value
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "sponsor_account_allowance_transactions", schema = "accounting")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SponsorAccountAllowanceTransactionsEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;
    @NonNull
    UUID accountId;
    @NonNull
    ZonedDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "transaction_type")
    @NonNull
    TransactionType type;

    @NonNull
    BigDecimal amount;

    UUID projectId;

    public SponsorAccount.AllowanceTransaction toDomain() {
        return new SponsorAccount.AllowanceTransaction(
                SponsorAccount.Transaction.Id.of(id),
                timestamp,
                type.toDomain(),
                PositiveAmount.of(amount),
                projectId == null ? null : ProjectId.of(projectId));
    }

    public static SponsorAccountAllowanceTransactionsEntity of(SponsorAccount.Id sponsorAccountId, SponsorAccount.AllowanceTransaction transaction) {
        return SponsorAccountAllowanceTransactionsEntity.builder()
                .id(transaction.id().value())
                .type(TransactionType.of(transaction.type()))
                .accountId(sponsorAccountId.value())
                .timestamp(transaction.timestamp())
                .amount(transaction.amount().getValue())
                .projectId(transaction.projectId() == null ? null : transaction.projectId().value())
                .build();
    }

    public enum TransactionType {
        MINT, BURN, TRANSFER, REFUND;

        public SponsorAccount.AllowanceTransaction.Type toDomain() {
            return switch (this) {
                case MINT -> SponsorAccount.AllowanceTransaction.Type.MINT;
                case BURN -> SponsorAccount.AllowanceTransaction.Type.BURN;
                case TRANSFER -> SponsorAccount.AllowanceTransaction.Type.TRANSFER;
                case REFUND -> SponsorAccount.AllowanceTransaction.Type.REFUND;
            };
        }

        public static TransactionType of(SponsorAccount.AllowanceTransaction.Type type) {
            return switch (type) {
                case MINT -> MINT;
                case BURN -> BURN;
                case TRANSFER -> TRANSFER;
                case REFUND -> REFUND;
            };
        }
    }
}


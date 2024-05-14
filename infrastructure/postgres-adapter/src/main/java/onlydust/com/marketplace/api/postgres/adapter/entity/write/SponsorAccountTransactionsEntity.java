package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
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
@Table(name = "sponsor_account_transactions", schema = "accounting")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SponsorAccountTransactionsEntity {
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

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "network")
    @NonNull
    NetworkEnumEntity network;

    @NonNull
    String reference;
    @NonNull
    BigDecimal amount;
    @NonNull
    String thirdPartyName;
    @NonNull
    String thirdPartyAccountNumber;

    public SponsorAccount.Transaction toDomain() {
        return new SponsorAccount.Transaction(
                SponsorAccount.Transaction.Id.of(id),
                timestamp,
                type.toDomain(),
                network.toNetwork(),
                reference,
                PositiveAmount.of(amount),
                thirdPartyName,
                thirdPartyAccountNumber);
    }

    public static SponsorAccountTransactionsEntity of(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction transaction) {
        return SponsorAccountTransactionsEntity.builder()
                .id(transaction.id().value())
                .timestamp(transaction.timestamp())
                .type(TransactionType.of(transaction.type()))
                .accountId(sponsorAccountId.value())
                .amount(transaction.amount().getValue())
                .network(NetworkEnumEntity.of(transaction.network()))
                .reference(transaction.reference())
                .thirdPartyAccountNumber(transaction.thirdPartyAccountNumber())
                .thirdPartyName(transaction.thirdPartyName())
                .build();
    }

    public enum TransactionType {
        DEPOSIT, WITHDRAW, SPEND;

        public SponsorAccount.Transaction.Type toDomain() {
            return switch (this) {
                case DEPOSIT -> SponsorAccount.Transaction.Type.DEPOSIT;
                case WITHDRAW -> SponsorAccount.Transaction.Type.WITHDRAW;
                case SPEND -> SponsorAccount.Transaction.Type.SPEND;
            };
        }

        public static TransactionType of(SponsorAccount.Transaction.Type type) {
            return switch (type) {
                case DEPOSIT -> DEPOSIT;
                case WITHDRAW -> WITHDRAW;
                case SPEND -> SPEND;
            };
        }
    }
}


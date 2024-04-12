package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "transaction_type", typeClass = PostgreSQLEnumType.class)
@Value
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "sponsor_account_transactions", schema = "accounting")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SponsorAccountTransactionsEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;
    @NonNull UUID accountId;

    @NonNull ZonedDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Type(type = "transaction_type")
    @NonNull TransactionType type;

    @Enumerated(EnumType.STRING)
    @Type(type = "network")
    @NonNull NetworkEnumEntity network;

    @NonNull String reference;
    @NonNull BigDecimal amount;
    @NonNull String thirdPartyName;
    @NonNull String thirdPartyAccountNumber;

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


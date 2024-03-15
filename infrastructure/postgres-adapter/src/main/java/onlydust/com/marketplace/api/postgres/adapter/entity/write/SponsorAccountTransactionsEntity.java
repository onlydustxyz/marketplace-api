package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
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
    @NonNull UUID id;
    @EqualsAndHashCode.Include
    @NonNull UUID accountId;

    @EqualsAndHashCode.Include
    @Enumerated(javax.persistence.EnumType.STRING)
    @Type(type = "transaction_type")
    @NonNull TransactionType type;

    @EqualsAndHashCode.Include
    @Enumerated(javax.persistence.EnumType.STRING)
    @Type(type = "network")
    @NonNull NetworkEnumEntity network;

    @EqualsAndHashCode.Include
    @NonNull String reference;
    @NonNull BigDecimal amount;
    @NonNull String thirdPartyName;
    @NonNull String thirdPartyAccountNumber;

    public SponsorAccount.Transaction toTransaction() {
        return new SponsorAccount.Transaction(
                SponsorAccount.Transaction.Id.of(id),
                type.toDomain(),
                network.toNetwork(),
                reference,
                Amount.of(amount),
                thirdPartyName,
                thirdPartyAccountNumber);
    }

    public static SponsorAccountTransactionsEntity of(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction transaction) {
        return SponsorAccountTransactionsEntity.builder()
                .id(transaction.id().value())
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
        DEPOSIT, SPEND;

        public SponsorAccount.Transaction.Type toDomain() {
            return switch (this) {
                case DEPOSIT -> SponsorAccount.Transaction.Type.DEPOSIT;
                case SPEND -> SponsorAccount.Transaction.Type.SPEND;
            };
        }

        public static TransactionType of(SponsorAccount.Transaction.Type type) {
            return switch (type) {
                case DEPOSIT -> DEPOSIT;
                case SPEND -> SPEND;
            };
        }
    }
}


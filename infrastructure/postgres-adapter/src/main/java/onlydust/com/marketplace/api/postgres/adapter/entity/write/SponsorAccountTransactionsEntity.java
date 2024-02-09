package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.NetworkEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
@Value
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "sponsor_account_transactions", schema = "accounting")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SponsorAccountTransactionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @EqualsAndHashCode.Include
    @NonNull UUID accountId;

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
        return new SponsorAccount.Transaction(network.toNetwork(), reference, Amount.of(amount), thirdPartyName, thirdPartyAccountNumber);
    }

    public static SponsorAccountTransactionsEntity of(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction transaction) {
        return SponsorAccountTransactionsEntity.builder()
                .accountId(sponsorAccountId.value())
                .amount(transaction.amount().getValue())
                .network(NetworkEnumEntity.of(transaction.network()))
                .reference(transaction.reference())
                .thirdPartyAccountNumber(transaction.thirdPartyAccountNumber())
                .thirdPartyName(transaction.thirdPartyName())
                .build();
    }
}


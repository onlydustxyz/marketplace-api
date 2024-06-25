package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.TransactionNetwork;
import onlydust.com.backoffice.api.contract.model.TransactionReceipt;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "sponsor_account_transactions", schema = "accounting")
@Immutable
@Accessors(fluent = true)
public class SponsorAccountTransactionReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @ManyToOne
    @JoinColumn(name = "accountId")
    @NonNull
    SponsorAccountReadEntity account;

    @NonNull
    @Enumerated(EnumType.STRING)
    NetworkEnumEntity network;

    @NonNull
    @Enumerated(EnumType.STRING)
    SponsorAccount.Transaction.Type type;

    @NonNull
    BigDecimal amount;

    @NonNull
    String reference;

    @NonNull
    String thirdPartyName;

    @NonNull
    String thirdPartyAccountNumber;

    @NonNull
    ZonedDateTime timestamp;

    public TransactionReceipt toDto() {
        return new TransactionReceipt()
                .id(id)
                .reference(reference)
                .network(map(network))
                .amount(type.isDebit() ? amount.negate() : amount)
                .thirdPartyName(thirdPartyName)
                .thirdPartyAccountNumber(thirdPartyAccountNumber);
    }

    private TransactionNetwork map(NetworkEnumEntity network) {
        return switch (network) {
            case sepa -> TransactionNetwork.SEPA;
            case ethereum -> TransactionNetwork.ETHEREUM;
            case aptos -> TransactionNetwork.APTOS;
            case starknet -> TransactionNetwork.STARKNET;
            case optimism -> TransactionNetwork.OPTIMISM;
        };
    }
}

package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Receipt;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.project.domain.view.ReceiptView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "receipts", schema = "accounting")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(fluent = true, chain = true)
@Immutable
public class ReceiptViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;
    @NonNull
    Date createdAt;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "network")
    @NonNull
    NetworkEnumEntity network;

    @NonNull
    String thirdPartyName;
    @NonNull
    String thirdPartyAccountNumber;
    @NonNull
    String transactionReference;

    public ReceiptView toView() {
        return ReceiptView.builder()
                .type(network == NetworkEnumEntity.sepa ? ReceiptView.Type.FIAT : ReceiptView.Type.CRYPTO)
                .blockchain(switch (network) {
                    case ethereum -> Blockchain.ETHEREUM;
                    case aptos -> Blockchain.APTOS;
                    case optimism -> Blockchain.OPTIMISM;
                    case starknet -> Blockchain.STARKNET;
                    default -> null;
                })
                .walletAddress(network == NetworkEnumEntity.sepa ? null : thirdPartyAccountNumber)
                .iban(network == NetworkEnumEntity.sepa ? thirdPartyAccountNumber : null)
                .transactionReference(transactionReference)
                .build();
    }

    public Receipt toDomain(RewardId rewardId) {
        return new Receipt(Receipt.Id.of(id),
                rewardId,
                createdAt.toInstant().atZone(ZoneOffset.UTC),
                network.toNetwork(),
                transactionReference,
                thirdPartyName,
                thirdPartyAccountNumber);
    }
}

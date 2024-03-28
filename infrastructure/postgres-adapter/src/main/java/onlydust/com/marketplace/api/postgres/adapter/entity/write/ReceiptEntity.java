package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Receipt;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.project.domain.view.ReceiptView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

@Entity
@Value
@Table(name = "receipts", schema = "accounting")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(fluent = true, chain = true)
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
public class ReceiptEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;
    @NonNull Date createdAt;

    @Type(type = "network")
    @Enumerated(EnumType.STRING)
    @NonNull NetworkEnumEntity network;

    @NonNull String thirdPartyName;
    @NonNull String thirdPartyAccountNumber;
    @NonNull String transactionReference;

    public static ReceiptEntity of(Receipt receipt) {
        return new ReceiptEntity(
                receipt.id().value(),
                Date.from(receipt.createdAt().toInstant()),
                NetworkEnumEntity.of(receipt.network()),
                receipt.thirdPartyName(),
                receipt.thirdPartyAccountNumber(),
                receipt.reference()
        );
    }

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

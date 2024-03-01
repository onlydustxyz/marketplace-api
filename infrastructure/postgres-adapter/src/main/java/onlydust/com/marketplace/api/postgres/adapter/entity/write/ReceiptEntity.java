package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.project.domain.view.ReceiptView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Value
@Table(name = "receipts", schema = "accounting")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(fluent = true, chain = true)
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
public class ReceiptEntity {
    @Id
    @NonNull UUID id;
    @NonNull Date createdAt;

    @Type(type = "network")
    @Enumerated(EnumType.STRING)
    @NonNull NetworkEnumEntity network;

    @NonNull String thirdPartyName;
    @NonNull String thirdPartyAccountNumber;
    @NonNull String transactionReference;

    public ReceiptView toDomain() {
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
}

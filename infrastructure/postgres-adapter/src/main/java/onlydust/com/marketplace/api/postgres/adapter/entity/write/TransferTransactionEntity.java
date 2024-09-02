package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmContractAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransferTransaction;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "transfer_transactions", schema = "accounting")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = true)
public class TransferTransactionEntity {
    @Id
    UUID id;

    @NonNull
    ZonedDateTime timestamp;

    @NonNull
    String reference;

    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    NetworkEnumEntity blockchain;

    @NonNull
    String senderAddress;

    @NonNull
    String recipientAddress;

    @NonNull
    BigDecimal amount;

    String contractAddress;

    public static TransferTransactionEntity of(Blockchain.TransferTransaction transaction) {
        return TransferTransactionEntity.builder()
                .id(UUID.randomUUID())
                .timestamp(transaction.timestamp())
                .reference(transaction.reference())
                .blockchain(NetworkEnumEntity.valueOf(transaction.blockchain().name()))
                .senderAddress(transaction.senderAddress())
                .recipientAddress(transaction.recipientAddress())
                .amount(transaction.amount())
                .contractAddress(transaction.contractAddress().orElse(null))
                .build();
    }

    public Blockchain.TransferTransaction toDomain() {
        return switch (blockchain) {
            case ETHEREUM, OPTIMISM -> new EvmTransferTransaction(
                    Blockchain.valueOf(blockchain.name()),
                    new EvmTransaction.Hash(reference),
                    timestamp,
                    Blockchain.Transaction.Status.CONFIRMED,
                    new EvmAccountAddress(senderAddress),
                    new EvmAccountAddress(recipientAddress),
                    amount,
                    contractAddress != null ? new EvmContractAddress(contractAddress) : null
            );
            default -> throw internalServerError("Unknown blockchain: " + blockchain);
        };
    }
}

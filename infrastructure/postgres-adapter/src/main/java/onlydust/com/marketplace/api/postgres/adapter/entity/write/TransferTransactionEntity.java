package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.kernel.model.blockchain.*;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosTransferTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransferTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransferTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransferTransaction;
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
            case SEPA -> throw internalServerError("SEPA transfer transactions are not supported");
            case ETHEREUM -> new EvmTransferTransaction(
                    Blockchain.valueOf(blockchain.name()),
                    Ethereum.transactionHash(reference),
                    timestamp,
                    Blockchain.Transaction.Status.CONFIRMED,
                    Ethereum.accountAddress(senderAddress),
                    Ethereum.accountAddress(recipientAddress),
                    amount,
                    contractAddress != null ? Ethereum.contractAddress(contractAddress) : null
            );
            case OPTIMISM -> new EvmTransferTransaction(
                    Blockchain.valueOf(blockchain.name()),
                    Optimism.transactionHash(reference),
                    timestamp,
                    Blockchain.Transaction.Status.CONFIRMED,
                    Optimism.accountAddress(senderAddress),
                    Optimism.accountAddress(recipientAddress),
                    amount,
                    contractAddress != null ? Optimism.contractAddress(contractAddress) : null
            );
            case APTOS -> new AptosTransferTransaction(
                    Aptos.transactionHash(reference),
                    timestamp,
                    Blockchain.Transaction.Status.CONFIRMED,
                    Aptos.accountAddress(senderAddress),
                    Aptos.accountAddress(recipientAddress),
                    amount,
                    contractAddress != null ? Aptos.coinType(contractAddress) : null
            );
            case STARKNET -> new StarknetTransferTransaction(
                    StarkNet.transactionHash(reference),
                    timestamp,
                    Blockchain.Transaction.Status.CONFIRMED,
                    StarkNet.accountAddress(senderAddress),
                    StarkNet.accountAddress(recipientAddress),
                    amount,
                    contractAddress != null ? StarkNet.contractAddress(contractAddress) : null
            );
            case STELLAR -> new StellarTransferTransaction(
                    Stellar.transactionHash(reference),
                    timestamp,
                    Blockchain.Transaction.Status.CONFIRMED,
                    Stellar.accountId(senderAddress),
                    Stellar.accountId(recipientAddress),
                    amount,
                    contractAddress != null ? Stellar.contractAddress(contractAddress) : null
            );
        };
    }
}

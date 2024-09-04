package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.backoffice.api.contract.model.TransactionNetwork;
import onlydust.com.backoffice.api.contract.model.TransferTransactionResponse;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "transfer_transactions", schema = "accounting")
@Immutable
@Accessors(fluent = true)
public class TransferTransactionReadEntity {
    @Id
    UUID id;

    @NonNull
    ZonedDateTime timestamp;

    @NonNull
    String reference;

    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    TransactionNetwork blockchain;

    @NonNull
    String senderAddress;

    @NonNull
    String recipientAddress;

    @NonNull
    BigDecimal amount;

    String contractAddress;

    private URI blockExplorerUrl() {
        try {
            return switch (blockchain) {
                case SEPA -> null;
                case ETHEREUM -> Blockchain.ETHEREUM.getBlockExplorerUrl(reference);
                case OPTIMISM -> Blockchain.OPTIMISM.getBlockExplorerUrl(reference);
                case STARKNET -> Blockchain.STARKNET.getBlockExplorerUrl(reference);
                case APTOS -> Blockchain.APTOS.getBlockExplorerUrl(reference);
                case STELLAR -> Blockchain.STELLAR.getBlockExplorerUrl(reference);
            };
        } catch (OnlyDustException e) {
            LOGGER.error("Error while generating block explorer URL for blockchain %s and reference %s".formatted(blockchain, reference), e);
            return null;
        }
    }

    public TransferTransactionResponse toBoResponse() {
        return new TransferTransactionResponse()
                .id(id)
                .network(blockchain)
                .reference(reference)
                .timestamp(timestamp)
                .amount(amount)
                .blockExplorerUrl(blockExplorerUrl());
    }
}

package onlydust.com.marketplace.api.stellar.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.stellar.SorobanClient;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarAccountId;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransferTransaction;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.sorobanrpc.GetTransactionResponse;
import org.stellar.sdk.xdr.OperationType;
import org.stellar.sdk.xdr.TransactionEnvelope;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.Transaction.Status.*;

@AllArgsConstructor
public class StellarTransactionStorageAdapter implements BlockchainTransactionStoragePort<StellarTransaction, StellarTransaction.Hash> {
    private final SorobanClient client;

    @Override
    public Optional<StellarTransaction> get(final @NonNull StellarTransaction.Hash reference) {
        return client.transaction(reference.toString())
                .map(t -> from(reference, t));
    }

    @SneakyThrows
    private static @NonNull StellarTransaction from(final @NonNull StellarTransaction.Hash reference,
                                                    final @NonNull GetTransactionResponse response) {
        final var tx = TransactionEnvelope.fromXdrBase64(response.getEnvelopeXdr()).getV1().getTx();

        return Arrays.stream(tx.getOperations())
                .filter(op -> op.getBody().getDiscriminant() == OperationType.PAYMENT)
                .map(op -> op.getBody().getPaymentOp())
                .map(op -> new StellarTransferTransaction(
                        reference,
                        Instant.ofEpochSecond(response.getCreatedAt()).atZone(ZoneOffset.UTC),
                        switch (response.getStatus()) {
                            case NOT_FOUND -> PENDING;
                            case SUCCESS -> CONFIRMED;
                            case FAILED -> FAILED;
                        },
                        StellarAccountId.of(KeyPair.fromPublicKey(tx.getSourceAccount().getEd25519().getUint256()).getAccountId()),
                        StellarAccountId.of(KeyPair.fromPublicKey(op.getDestination().getEd25519().getUint256()).getAccountId()),
                        BigDecimal.valueOf(op.getAmount().getInt64(), 7),
                        null))
                .findFirst()
                .orElseThrow(() -> badRequest("Transaction %s does not contain any payment operation".formatted(reference)));
    }
}

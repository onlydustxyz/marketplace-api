package onlydust.com.marketplace.api.stellar.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.stellar.HorizonClient;
import onlydust.com.marketplace.api.stellar.SorobanClient;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarAccountId;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarContractAddress;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransferTransaction;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.sorobanrpc.GetTransactionResponse;
import org.stellar.sdk.scval.Scv;
import org.stellar.sdk.xdr.OperationType;
import org.stellar.sdk.xdr.TransactionEnvelope;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.Transaction.Status.*;

@AllArgsConstructor
public class StellarTransactionStorageAdapter implements BlockchainTransactionStoragePort<StellarTransaction, StellarTransaction.Hash> {
    private final SorobanClient soroban;
    private final HorizonClient horizon;

    @Override
    public Optional<StellarTransaction> get(final @NonNull StellarTransaction.Hash reference) {
        return soroban.transaction(reference.toString())
                .map(t -> from(reference, t));
    }

    @SneakyThrows
    private @NonNull StellarTransaction from(final @NonNull StellarTransaction.Hash reference,
                                             final @NonNull GetTransactionResponse response) {

        final var envelope = TransactionEnvelope.fromXdrBase64(Optional.ofNullable(response.getEnvelopeXdr())
                .orElseThrow(() -> internalServerError("Transaction %s has no envelope".formatted(reference))));

        final var tx = switch (envelope.getDiscriminant()) {
            case ENVELOPE_TYPE_TX -> envelope.getV1().getTx();
            default -> throw internalServerError("Transaction %s envelope type is not supported: %s".formatted(reference, envelope.getDiscriminant()));
        };

        return Arrays.stream(tx.getOperations())
                .filter(op -> op.getBody().getDiscriminant() == OperationType.PAYMENT)
                .map(op -> op.getBody().getPaymentOp())
                .map(op -> {
                    final var contractAddress = switch (op.getAsset().getDiscriminant()) {
                        case ASSET_TYPE_CREDIT_ALPHANUM4, ASSET_TYPE_CREDIT_ALPHANUM12 -> StellarContractAddress.of(horizon.asset(op.getAsset())
                                .orElseThrow(() -> badRequest("Asset not found"))
                                .getContractID());
                        default -> null;
                    };

                    final Long decimals = contractAddress == null ? 7 : Scv.fromUint32(soroban.call(contractAddress.toString(), "decimals"));

                    return new StellarTransferTransaction(
                            reference,
                            Instant.ofEpochSecond(response.getCreatedAt()).atZone(ZoneOffset.UTC),
                            switch (response.getStatus()) {
                                case NOT_FOUND -> PENDING;
                                case SUCCESS -> CONFIRMED;
                                case FAILED -> FAILED;
                            },
                            StellarAccountId.of(KeyPair.fromPublicKey(tx.getSourceAccount().getEd25519().getUint256()).getAccountId()),
                            StellarAccountId.of(KeyPair.fromPublicKey(op.getDestination().getEd25519().getUint256()).getAccountId()),
                            BigDecimal.valueOf(op.getAmount().getInt64(), decimals.intValue()),
                            contractAddress
                    );
                })
                .findFirst()
                .orElseThrow(() -> badRequest("Transaction %s does not contain any payment operation".formatted(reference)));
    }
}

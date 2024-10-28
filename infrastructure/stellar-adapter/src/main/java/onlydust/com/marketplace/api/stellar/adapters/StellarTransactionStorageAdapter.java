package onlydust.com.marketplace.api.stellar.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.stellar.HorizonClient;
import onlydust.com.marketplace.kernel.model.blockchain.Stellar;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarAccountId;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransferTransaction;
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.Transaction.Status.CONFIRMED;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.Transaction.Status.PENDING;

@AllArgsConstructor
public class StellarTransactionStorageAdapter implements BlockchainTransactionStoragePort<StellarTransaction, StellarTransaction.Hash> {
    private final HorizonClient horizon;

    @Override
    public Optional<StellarTransaction> get(final @NonNull StellarTransaction.Hash reference) {
        return horizon.payments(reference.toString()).stream()
                .findFirst()
                .map(this::from);
    }

    private StellarTransaction from(OperationResponse response) {
        if (response instanceof PaymentOperationResponse operation)
            return from(operation);
        if (response instanceof CreateAccountOperationResponse operation)
            return from(operation);

        throw internalServerError("Operation type %s is not supported".formatted(response.getType()));
    }

    @SneakyThrows
    private StellarTransferTransaction from(PaymentOperationResponse op) {
        final var contractAddress = switch (op.getAsset().getType()) {
            case "credit_alphanum4", "credit_alphanum12" -> Stellar.contractAddress(horizon.contractId(op.getAsset()));
            case "native" -> null;
            default -> throw internalServerError("Asset type %s is not supported".formatted(op.getAsset().getType()));
        };

        return new StellarTransferTransaction(
                Stellar.transactionHash(op.getTransactionHash()),
                ZonedDateTime.parse(op.getCreatedAt()),
                op.isTransactionSuccessful() ? CONFIRMED : PENDING,
                StellarAccountId.of(op.getFrom()),
                StellarAccountId.of(op.getTo()),
                new BigDecimal(op.getAmount()),
                contractAddress
        );
    }

    private StellarTransferTransaction from(CreateAccountOperationResponse op) {
        return new StellarTransferTransaction(
                Stellar.transactionHash(op.getTransactionHash()),
                ZonedDateTime.parse(op.getCreatedAt()),
                op.isTransactionSuccessful() ? CONFIRMED : PENDING,
                StellarAccountId.of(op.getSourceAccount()),
                StellarAccountId.of(op.getAccount()),
                new BigDecimal(op.getStartingBalance()),
                null
        );
    }
}

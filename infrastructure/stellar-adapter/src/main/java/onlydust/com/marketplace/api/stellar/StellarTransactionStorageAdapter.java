package onlydust.com.marketplace.api.stellar;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransaction;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

@AllArgsConstructor
public class StellarTransactionStorageAdapter implements BlockchainTransactionStoragePort<StellarTransaction, StellarTransaction.Hash> {
    private final StellarClient client;

    @Override
    public Optional<StellarTransaction> get(final @NonNull StellarTransaction.Hash reference) {
        return client.transaction(reference.toString())
                .map(t -> new StellarTransaction(
                        reference,
                        Instant.ofEpochSecond(t.getCreatedAt()).atZone(ZoneOffset.UTC)));
    }
}

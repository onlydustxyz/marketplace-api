package onlydust.com.marketplace.api.stellar;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.kernel.model.blockchain.Stellar;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransaction;

import java.time.ZonedDateTime;
import java.util.Optional;

@AllArgsConstructor
public class StellarTransactionStorageAdapter implements BlockchainTransactionStoragePort<StellarTransaction, StellarTransaction.Hash> {
    private final StellarClient client;

    @Override
    public Optional<StellarTransaction> get(StellarTransaction.Hash reference) {
        return client.transaction(reference.toString())
                .map(t -> new StellarTransaction(
                        Stellar.transactionHash(t.getHash()),
                        ZonedDateTime.parse(t.getCreatedAt())));
    }
}

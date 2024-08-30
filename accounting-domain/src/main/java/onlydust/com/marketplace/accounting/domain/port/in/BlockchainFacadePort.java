package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

import java.util.Optional;

public interface BlockchainFacadePort {
    Optional<Blockchain.Transaction> getTransaction(Blockchain blockchain, String reference);

    String sanitizedTransactionReference(Blockchain blockchain, String reference);
}

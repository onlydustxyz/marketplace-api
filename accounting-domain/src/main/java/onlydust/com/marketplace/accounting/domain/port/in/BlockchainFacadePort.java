package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

import java.time.ZonedDateTime;

public interface BlockchainFacadePort {
    ZonedDateTime getTransactionTimestamp(Blockchain blockchain, String reference);
}

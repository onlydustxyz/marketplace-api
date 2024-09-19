package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

public interface TransactionStoragePort {
    boolean exists(final @NonNull Blockchain blockchain, final @NonNull String transactionReference);
}

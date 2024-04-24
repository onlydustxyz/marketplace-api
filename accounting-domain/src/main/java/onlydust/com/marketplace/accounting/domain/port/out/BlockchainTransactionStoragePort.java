package onlydust.com.marketplace.accounting.domain.port.out;

import java.util.Optional;

public interface BlockchainTransactionStoragePort<TRX, REF> {
    Optional<TRX> get(REF reference);
}

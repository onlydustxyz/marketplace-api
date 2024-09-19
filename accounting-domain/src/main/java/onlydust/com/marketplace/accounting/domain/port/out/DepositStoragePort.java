package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Deposit;

import java.util.Optional;

public interface DepositStoragePort {
    void save(Deposit receipt);

    Optional<Deposit> find(Deposit.Id depositId);

    Optional<Deposit> findByTransactionReference(@NonNull String transactionReference);
}

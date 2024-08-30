package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.kernel.model.SponsorId;

import java.util.Optional;

public interface DepositStoragePort {
    void save(Deposit receipt);

    Optional<Deposit> find(Deposit.Id depositId);

    Optional<Deposit.BillingInformation> findLatestBillingInformation(@NonNull SponsorId sponsorId);

    Optional<Deposit> findByTransactionReference(@NonNull String transactionReference);
}

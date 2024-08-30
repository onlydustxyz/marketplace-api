package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.kernel.model.SponsorId;

import java.util.Optional;

public interface DepositStoragePort {
    void save(Deposit receipt);

    Optional<SponsorId> findDepositSponsor(Deposit.Id depositId);

    void saveStatusAndBillingInformation(Deposit.Id depositId, Deposit.Status status, Deposit.BillingInformation billingInformation);

    Optional<Deposit.BillingInformation> findLatestBillingInformation(@NonNull SponsorId sponsorId);
}

package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.kernel.model.UserId;

public interface DepositObserverPort {
    void onDepositSubmittedByUser(UserId userId, Deposit deposit);

    void onDepositRejected(Deposit deposit);

    void onDepositApproved(Deposit deposit);
}

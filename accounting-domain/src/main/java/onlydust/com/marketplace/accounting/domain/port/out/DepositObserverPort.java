package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.kernel.model.UserId;

public interface DepositObserverPort {
    void onDepositSubmittedByUser(UserId userId, Deposit.Id depositId);

    void onDepositRejected(Deposit.Id depositId);

    void onDepositApproved(Deposit.Id depositId);
}

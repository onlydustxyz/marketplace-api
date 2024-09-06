package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.port.out.DepositObserverPort;
import onlydust.com.marketplace.kernel.model.UserId;

import java.util.List;

public class DepositObserverComposite implements DepositObserverPort {

    private final List<DepositObserverPort> depositObservers;

    public DepositObserverComposite(DepositObserverPort... depositObservers) {
        this.depositObservers = List.of(depositObservers);
    }

    @Override
    public void onDepositSubmittedByUser(UserId userId, Deposit.Id depositId) {
        depositObservers.forEach(observer -> observer.onDepositSubmittedByUser(userId, depositId));
    }

    @Override
    public void onDepositRejected(Deposit.Id depositId) {
        depositObservers.forEach(observer -> observer.onDepositRejected(depositId));
    }

    @Override
    public void onDepositApproved(Deposit.Id depositId) {
        depositObservers.forEach(observer -> observer.onDepositApproved(depositId));
    }
}

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
    public void onDepositSubmittedByUser(UserId userId, Deposit deposit) {
        depositObservers.forEach(observer -> observer.onDepositSubmittedByUser(userId, deposit));
    }

    @Override
    public void onDepositRejected(Deposit deposit) {
        depositObservers.forEach(observer -> observer.onDepositRejected(deposit));
    }

    @Override
    public void onDepositApproved(Deposit deposit) {
        depositObservers.forEach(observer -> observer.onDepositApproved(deposit));
    }
}

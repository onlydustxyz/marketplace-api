package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Deposit;

public interface DepositStoragePort {
    void save(Deposit receipt);
}

package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookTransactionProjection;

public interface AccountBookStorage {
    void save(AccountBookTransactionProjection projection);
}

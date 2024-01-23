package onlydust.com.marketplace.accounting.domain.model.accountbook;

import onlydust.com.marketplace.accounting.domain.model.AccountId;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;

public interface AccountBook {
    void mint(AccountId account, PositiveAmount amount);

    void burn(AccountId account, PositiveAmount amount);

    void transfer(AccountId from, AccountId to, PositiveAmount amount);

    void refund(AccountId from, AccountId to, PositiveAmount amount);
}

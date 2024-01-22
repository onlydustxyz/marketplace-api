package onlydust.com.marketplace.accounting.domain.model.accountbook;

import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;

public interface AccountBook {
    void mint(Account.Id account, PositiveAmount amount);

    void burn(Account.Id account, PositiveAmount amount);

    void transfer(Account.Id from, Account.Id to, PositiveAmount amount);

    void refund(Account.Id from, Account.Id to, PositiveAmount amount);
}

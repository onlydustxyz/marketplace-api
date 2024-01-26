package onlydust.com.marketplace.accounting.domain.model.accountbook;

import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;

public interface AccountBook {
    void mint(Ledger.Id account, PositiveAmount amount);

    void burn(Ledger.Id account, PositiveAmount amount);

    void transfer(Ledger.Id from, Ledger.Id to, PositiveAmount amount);

    void refund(Ledger.Id from, Ledger.Id to, PositiveAmount amount);
}

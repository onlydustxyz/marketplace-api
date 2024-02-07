package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;

public interface SponsorAccountObserver {
    void onSponsorAccountBalanceChanged(SponsorAccount sponsorAccount, AccountBookAggregate accountBook);
}

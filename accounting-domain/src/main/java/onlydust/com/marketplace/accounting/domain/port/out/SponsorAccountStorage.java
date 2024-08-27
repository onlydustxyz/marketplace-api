package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.kernel.model.SponsorId;

import java.util.List;

public interface SponsorAccountStorage extends SponsorAccountProvider {
    void save(SponsorAccount... sponsorAccounts);

    void delete(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction.Id transactionId);

    List<SponsorAccount> getSponsorAccounts(SponsorId sponsorId);

    List<SponsorAccount> find(SponsorId sponsorId, Currency.Id currencyId);
}

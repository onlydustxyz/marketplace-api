package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

import java.util.List;

public interface SponsorAccountStorage extends SponsorAccountProvider {
    void save(SponsorAccount... sponsorAccounts);

    void deleteTransaction(SponsorAccount.Id sponsorAccountId, String reference);

    List<SponsorAccount> getSponsorAccounts(SponsorId sponsorId);
}

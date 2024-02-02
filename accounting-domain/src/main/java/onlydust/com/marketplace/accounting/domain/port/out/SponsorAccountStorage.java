package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;

import java.util.Optional;

public interface SponsorAccountStorage {
    Optional<SponsorAccount> get(SponsorAccount.Id id);

    void save(SponsorAccount... sponsorAccounts);

    void deleteTransaction(String reference);
}

package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

import java.util.Optional;

public interface SponsorAccountProvider {
    Optional<Account> sponsorAccount(SponsorId sponsorId);
}

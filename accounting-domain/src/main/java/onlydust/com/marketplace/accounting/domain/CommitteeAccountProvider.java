package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.CommitteeId;
import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.util.Optional;

public interface CommitteeAccountProvider {
    Optional<Account> get(CommitteeId committeeId, Currency currency);
}

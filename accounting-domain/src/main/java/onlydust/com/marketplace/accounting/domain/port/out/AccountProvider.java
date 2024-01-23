package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.util.Optional;

public interface AccountProvider<ID> {
    Optional<Account.Id> get(ID ownerId, Currency currency);

    Account.Id create(ID ownerId, Currency currency);
}

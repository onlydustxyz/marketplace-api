package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.AccountId;
import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.util.Optional;

public interface AccountProvider<ID> {
    Optional<AccountId> get(ID ownerId, Currency currency);

    AccountId create(ID ownerId, Currency currency);
}

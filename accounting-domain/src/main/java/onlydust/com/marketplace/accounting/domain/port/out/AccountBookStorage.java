package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookTransactionProjection;

import java.util.Optional;

public interface AccountBookStorage {
    void save(AccountBookTransactionProjection projection);

    Optional<AccountBookAggregate> get(Currency.Id currencyId);
}

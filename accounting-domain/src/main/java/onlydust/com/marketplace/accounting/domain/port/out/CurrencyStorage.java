package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;

public interface CurrencyStorage {
    void save(Currency currency);
}

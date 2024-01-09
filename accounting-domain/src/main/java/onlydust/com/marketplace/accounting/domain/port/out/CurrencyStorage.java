package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.util.Optional;

public interface CurrencyStorage {
    void save(Currency currency);

    Boolean exists(Currency.Code code);
}
